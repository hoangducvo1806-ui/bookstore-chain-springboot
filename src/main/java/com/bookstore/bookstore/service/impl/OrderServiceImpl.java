package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.dto.CheckoutForm;
import com.bookstore.bookstore.entity.*;
import com.bookstore.bookstore.enums.OrderStatus;
import com.bookstore.bookstore.enums.PaymentMethod;
import com.bookstore.bookstore.enums.PaymentStatus;
import com.bookstore.bookstore.repository.*;
import com.bookstore.bookstore.service.AddressService;
import com.bookstore.bookstore.service.CartService;
import com.bookstore.bookstore.service.NotificationService;
import com.bookstore.bookstore.service.OrderService;
import com.bookstore.bookstore.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final BookRepository bookRepository;
    private final ShippingCarrierRepository shippingCarrierRepository;
    private final ShopRepository shopRepository;
    private final CartService cartService;
    private final AddressService addressService;
    private final VoucherService voucherService;
    private final NotificationService notificationService;

    @Override
    public List<Order> checkout(User customer, CheckoutForm form) {
        Cart cart = cartService.getOrCreateCart(customer);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng của bạn đang trống!");
        }

        // 1. Xác định địa chỉ giao hàng
        String recipientName = form.getRecipientName();
        String recipientPhone = form.getRecipientPhone();
        String shippingAddress = form.getShippingAddress();

        if (form.getAddressId() != null) {
            Address address = addressService.getAddressById(form.getAddressId(), customer);
            recipientName = address.getRecipientName();
            recipientPhone = address.getPhone();
            shippingAddress = address.getStreetAddress() + ", " + address.getWard() + ", " 
                    + address.getDistrict() + ", " + address.getProvince();
        }

        if (recipientName == null || recipientName.trim().isEmpty() ||
            recipientPhone == null || recipientPhone.trim().isEmpty() ||
            shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp đầy đủ thông tin địa chỉ và số điện thoại nhận hàng!");
        }

        // 2. Xác định đơn vị vận chuyển
        ShippingCarrier carrier = shippingCarrierRepository.findById(form.getCarrierId())
                .orElseThrow(() -> new IllegalArgumentException("Vui lòng chọn đơn vị vận chuyển hợp lệ!"));

        // 3. Kiểm tra voucher nếu có
        BigDecimal totalSubtotal = cart.getItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Voucher voucher = null;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        if (form.getVoucherCode() != null && !form.getVoucherCode().trim().isEmpty()) {
            voucher = voucherService.validateAndGetVoucher(form.getVoucherCode().trim(), totalSubtotal);
            totalDiscount = voucherService.calculateDiscount(voucher, totalSubtotal);
        }

        // 4. Nhóm cart items theo từng shop để tạo đơn hàng tương ứng
        Map<Long, List<CartItem>> itemsByShop = new HashMap<>();
        for (CartItem item : cart.getItems()) {
            Book book = item.getBook();
            if (book.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Sách '" + book.getTitle() + "' chỉ còn " 
                        + book.getStock() + " cuốn, không đủ để đặt hàng!");
            }
            Long shopId = book.getShop() != null ? book.getShop().getId() : 0L;
            itemsByShop.computeIfAbsent(shopId, k -> new ArrayList<>()).add(item);
        }

        List<Order> createdOrders = new ArrayList<>();
        int shopCount = itemsByShop.size();
        BigDecimal remainingDiscount = totalDiscount;
        int currentShopIndex = 0;

        for (Map.Entry<Long, List<CartItem>> entry : itemsByShop.entrySet()) {
            currentShopIndex++;
            List<CartItem> shopItems = entry.getValue();
            Shop shop = shopItems.get(0).getBook().getShop();

            BigDecimal shopSubtotal = shopItems.stream()
                    .map(it -> it.getBook().getPrice().multiply(BigDecimal.valueOf(it.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Phân bổ giảm giá cho từng shop
            BigDecimal shopDiscount;
            if (currentShopIndex == shopCount) {
                shopDiscount = remainingDiscount;
            } else {
                shopDiscount = totalSubtotal.compareTo(BigDecimal.ZERO) > 0
                        ? totalDiscount.multiply(shopSubtotal).divide(totalSubtotal, 0, BigDecimal.ROUND_HALF_UP)
                        : BigDecimal.ZERO;
                remainingDiscount = remainingDiscount.subtract(shopDiscount);
            }

            BigDecimal shippingFee = carrier.getShippingFee();
            BigDecimal totalAmount = shopSubtotal.add(shippingFee).subtract(shopDiscount);
            if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                totalAmount = BigDecimal.ZERO;
            }

            String orderCode = generateOrderCode();

            Order order = Order.builder()
                    .orderCode(orderCode)
                    .customer(customer)
                    .shop(shop)
                    .status(OrderStatus.NEW)
                    .paymentMethod(form.getPaymentMethod())
                    .paymentStatus(PaymentStatus.PENDING)
                    .subtotal(shopSubtotal)
                    .shippingFee(shippingFee)
                    .discountAmount(shopDiscount)
                    .totalAmount(totalAmount)
                    .recipientName(recipientName)
                    .recipientPhone(recipientPhone)
                    .shippingAddress(shippingAddress)
                    .note(form.getNote())
                    .build();

            order = orderRepository.save(order);

            // Tạo các OrderItem và trừ tồn kho
            for (CartItem ci : shopItems) {
                Book book = ci.getBook();
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .book(book)
                        .quantity(ci.getQuantity())
                        .price(book.getPrice())
                        .build();
                orderItemRepository.save(orderItem);

                // Cập nhật tồn kho và số lượng đã bán
                book.setStock(book.getStock() - ci.getQuantity());
                book.setSoldQuantity((book.getSoldQuantity() != null ? book.getSoldQuantity() : 0) + ci.getQuantity());
                bookRepository.save(book);
            }

            // Ghi nhận lịch sử trạng thái đầu tiên
            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)
                    .status(OrderStatus.NEW)
                    .note("Đơn hàng mới tạo thành công")
                    .updatedBy(customer)
                    .build();
            orderStatusHistoryRepository.save(history);

            // Tạo bản ghi Payment
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(form.getPaymentMethod())
                    .amount(totalAmount)
                    .paymentStatus(PaymentStatus.PENDING)
                    .paymentInfo("Thanh toán đơn hàng " + orderCode)
                    .build();
            paymentRepository.save(payment);

            // Bắn WebSocket thông báo đơn hàng mới cho Shop và hệ thống
            notificationService.sendOrderNotification(order, "ORDER_CREATED", "Có đơn hàng mới " + orderCode + " cần xử lý!");

            createdOrders.add(order);
        }

        // 5. Cập nhật lượt dùng voucher nếu có
        if (voucher != null) {
            voucherService.recordVoucherUsage(voucher);
        }

        // 6. Xóa giỏ hàng
        cartService.clearCart(customer);

        return createdOrders;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserAndTab(User customer, String tab) {
        if (tab == null || tab.isEmpty() || "all".equalsIgnoreCase(tab)) {
            return orderRepository.findByCustomerOrderByCreatedAtDesc(customer);
        }

        return switch (tab.toLowerCase()) {
            case "new" -> orderRepository.findByCustomerAndStatusOrderByCreatedAtDesc(customer, OrderStatus.NEW);
            case "confirmed" -> orderRepository.findByCustomerAndStatusOrderByCreatedAtDesc(customer, OrderStatus.CONFIRMED);
            case "shipping" -> orderRepository.findByCustomerAndStatusInOrderByCreatedAtDesc(customer, 
                    List.of(OrderStatus.PICKED_UP, OrderStatus.SHIPPING));
            case "delivered" -> orderRepository.findByCustomerAndStatusOrderByCreatedAtDesc(customer, OrderStatus.DELIVERED);
            case "cancelled" -> orderRepository.findByCustomerAndStatusOrderByCreatedAtDesc(customer, OrderStatus.CANCELLED);
            case "returned" -> orderRepository.findByCustomerAndStatusInOrderByCreatedAtDesc(customer, 
                    List.of(OrderStatus.RETURN_REQUESTED, OrderStatus.RETURNED_REFUNDED));
            default -> orderRepository.findByCustomerOrderByCreatedAtDesc(customer);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByCode(String orderCode, User customer) {
        return orderRepository.findByOrderCodeAndCustomer(orderCode, customer)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng: " + orderCode));
    }

    @Override
    public void cancelOrder(String orderCode, User customer, String reason) {
        Order order = getOrderByCode(orderCode, customer);

        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalArgumentException("Chỉ có thể hủy đơn khi đơn hàng đang ở trạng thái 'Đơn mới'!");
        }

        // Kiểm tra điều kiện thời gian 1 tiếng theo đặc tả
        if (order.getCreatedAt() != null && order.getCreatedAt().plusHours(1).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Đã quá thời hạn 1 giờ kể từ khi đặt hàng, bạn không thể tự hủy đơn!");
        }

        // Hoàn lại tồn kho
        for (OrderItem item : order.getItems()) {
            Book book = item.getBook();
            book.setStock(book.getStock() + item.getQuantity());
            book.setSoldQuantity(Math.max(0, (book.getSoldQuantity() != null ? book.getSoldQuantity() : 0) - item.getQuantity()));
            bookRepository.save(book);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.CANCELLED)
                .note(reason != null && !reason.trim().isEmpty() ? reason.trim() : "Khách hàng yêu cầu hủy đơn")
                .updatedBy(customer)
                .build();
        orderStatusHistoryRepository.save(history);

        notificationService.sendOrderNotification(order, "ORDER_CANCELLED", "Khách hàng đã hủy đơn " + order.getOrderCode());
    }

    @Override
    public void requestReturnOrder(String orderCode, User customer, String reason) {
        Order order = getOrderByCode(orderCode, customer);

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Chỉ có thể gửi yêu cầu trả hàng khi đơn hàng đã được giao thành công!");
        }

        order.setStatus(OrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.RETURN_REQUESTED)
                .note(reason != null && !reason.trim().isEmpty() ? reason.trim() : "Khách hàng gửi yêu cầu trả hàng / hoàn tiền")
                .updatedBy(customer)
                .build();
        orderStatusHistoryRepository.save(history);

        notificationService.sendOrderNotification(order, "ORDER_RETURN_REQUESTED", "Khách hàng yêu cầu hoàn trả đơn " + order.getOrderCode());
    }

    @Override
    public void updatePaymentSuccess(String orderCode, String transactionId, String paymentInfo) {
        Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
        if (order != null) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            orderRepository.save(order);

            List<Payment> payments = paymentRepository.findByOrder(order);
            if (!payments.isEmpty()) {
                Payment payment = payments.get(0);
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setTransactionId(transactionId);
                payment.setPaymentInfo(paymentInfo);
                payment.setPaymentTime(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        }
    }

    // =========================================================================
    // PHÂN HỆ VENDOR / SHOP QUẢN LÝ ĐƠN HÀNG (TV2)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByShopAndTab(User vendor, String tab, int page, int size) {
        Shop shop = shopRepository.findByOwner(vendor)
                .orElseThrow(() -> new IllegalArgumentException("Bạn chưa sở hữu cửa hàng nào!"));

        Pageable pageable = PageRequest.of(Math.max(0, page), size > 0 ? size : 10);

        if (tab == null || tab.isBlank() || "all".equalsIgnoreCase(tab)) {
            return orderRepository.findByShopOrderByCreatedAtDesc(shop, pageable);
        }

        return switch (tab.toLowerCase()) {
            case "new", "not_processed" -> orderRepository.findByShopAndStatusOrderByCreatedAtDesc(shop, OrderStatus.NEW, pageable);
            case "confirmed", "processing" -> orderRepository.findByShopAndStatusOrderByCreatedAtDesc(shop, OrderStatus.CONFIRMED, pageable);
            case "picked_up" -> orderRepository.findByShopAndStatusOrderByCreatedAtDesc(shop, OrderStatus.PICKED_UP, pageable);
            case "shipping", "shipped" -> orderRepository.findByShopAndStatusOrderByCreatedAtDesc(shop, OrderStatus.SHIPPING, pageable);
            case "delivered" -> orderRepository.findByShopAndStatusOrderByCreatedAtDesc(shop, OrderStatus.DELIVERED, pageable);
            case "cancelled" -> orderRepository.findByShopAndStatusOrderByCreatedAtDesc(shop, OrderStatus.CANCELLED, pageable);
            case "returned" -> orderRepository.findByShopAndStatusInOrderByCreatedAtDesc(shop, 
                    List.of(OrderStatus.RETURN_REQUESTED, OrderStatus.RETURNED_REFUNDED), pageable);
            default -> orderRepository.findByShopOrderByCreatedAtDesc(shop, pageable);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByShopAndId(User vendor, Long orderId) {
        Shop shop = shopRepository.findByOwner(vendor)
                .orElseThrow(() -> new IllegalArgumentException("Bạn chưa sở hữu cửa hàng nào!"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        if (!order.getShop().getId().equals(shop.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập đơn hàng của chi nhánh khác!");
        }

        return order;
    }

    @Override
    public void updateOrderStatusByVendor(User vendor, Long orderId, OrderStatus newStatus, String note) {
        Order order = getOrderByShopAndId(vendor, orderId);
        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Đơn hàng đã kết thúc ở trạng thái '" + currentStatus.getLabel() + "', không thể thay đổi nữa!");
        }

        // Quy trình State Machine hợp lệ cho Vendor:
        // NEW -> CONFIRMED (Đã xác nhận / Processing)
        // CONFIRMED -> PICKED_UP (Đã đóng gói, sẵn sàng lấy hàng)
        // NEW/CONFIRMED -> CANCELLED (Shop hủy đơn)
        // RETURN_REQUESTED -> RETURNED_REFUNDED (Shop chấp thuận hoàn tiền)
        if (newStatus == OrderStatus.CONFIRMED && currentStatus != OrderStatus.NEW) {
            throw new IllegalArgumentException("Chỉ đơn 'Mới' mới có thể chuyển sang 'Đã xác nhận'!");
        } else if (newStatus == OrderStatus.PICKED_UP && currentStatus != OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("Chỉ đơn 'Đã xác nhận' mới có thể chuyển sang 'Đã lấy hàng / Đóng gói xong'!");
        } else if (newStatus == OrderStatus.CANCELLED) {
            if (currentStatus != OrderStatus.NEW && currentStatus != OrderStatus.CONFIRMED) {
                throw new IllegalArgumentException("Không thể hủy đơn hàng khi đã bàn giao vận chuyển!");
            }
            // Hoàn lại tồn kho sách
            for (OrderItem item : order.getItems()) {
                Book book = item.getBook();
                book.setStock(book.getStock() + item.getQuantity());
                book.setSoldQuantity(Math.max(0, (book.getSoldQuantity() != null ? book.getSoldQuantity() : 0) - item.getQuantity()));
                bookRepository.save(book);
            }
        } else if (newStatus == OrderStatus.RETURNED_REFUNDED && currentStatus != OrderStatus.RETURN_REQUESTED) {
            throw new IllegalArgumentException("Chỉ đơn có yêu cầu trả hàng mới có thể chuyển sang 'Hoàn tiền'!");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(newStatus)
                .note(note != null && !note.trim().isEmpty() ? note.trim() : "Shop cập nhật trạng thái đơn sang: " + newStatus.getLabel())
                .updatedBy(vendor)
                .build();
        orderStatusHistoryRepository.save(history);

        notificationService.sendOrderNotification(order, "Shop " + order.getShop().getName() + " đã cập nhật trạng thái đơn " 
                + order.getOrderCode() + " thành: " + newStatus.getLabel());
    }

    // =========================================================================
    // PHÂN HỆ SHIPPER GIAO NHẬN ĐƠN HÀNG (TV2)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByShipperAndTab(User shipper, String tab, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size > 0 ? size : 10);

        if (tab == null || tab.isBlank() || "all".equalsIgnoreCase(tab)) {
            return orderRepository.findByShipperOrderByCreatedAtDesc(shipper, pageable);
        }

        return switch (tab.toLowerCase()) {
            case "picked_up" -> orderRepository.findByShipperAndStatusOrderByCreatedAtDesc(shipper, OrderStatus.PICKED_UP, pageable);
            case "shipping" -> orderRepository.findByShipperAndStatusOrderByCreatedAtDesc(shipper, OrderStatus.SHIPPING, pageable);
            case "delivered" -> orderRepository.findByShipperAndStatusOrderByCreatedAtDesc(shipper, OrderStatus.DELIVERED, pageable);
            case "returned" -> orderRepository.findByShipperAndStatusOrderByCreatedAtDesc(shipper, OrderStatus.RETURN_REQUESTED, pageable);
            default -> orderRepository.findByShipperOrderByCreatedAtDesc(shipper, pageable);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getAvailableOrdersForShipper(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size > 0 ? size : 10);
        // Lấy các đơn đã đóng gói / lấy hàng xong (PICKED_UP) hoặc CONFIRMED mà chưa có shipper
        return orderRepository.findByStatusAndShipperIsNullOrderByCreatedAtDesc(OrderStatus.CONFIRMED, pageable);
    }

    @Override
    public void claimOrder(User shipper, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng: " + orderId));

        if (order.getShipper() != null) {
            throw new IllegalArgumentException("Đơn hàng này đã có shipper khác phụ trách!");
        }

        order.setShipper(shipper);
        order.setStatus(OrderStatus.PICKED_UP);
        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.PICKED_UP)
                .note("Shipper " + shipper.getFullName() + " đã nhận đơn giao")
                .updatedBy(shipper)
                .build();
        orderStatusHistoryRepository.save(history);

        notificationService.sendOrderNotification(order, "Shipper " + shipper.getFullName() + " đã tiếp nhận đơn " + order.getOrderCode());
    }

    @Override
    public void updateOrderStatusByShipper(User shipper, Long orderId, OrderStatus newStatus, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng: " + orderId));

        if (order.getShipper() == null || !order.getShipper().getId().equals(shipper.getId())) {
            throw new IllegalArgumentException("Bạn không phải shipper được phân công cho đơn hàng này!");
        }

        OrderStatus currentStatus = order.getStatus();

        // Shipper State Machine:
        // PICKED_UP -> SHIPPING (Đang giao hàng)
        // SHIPPING -> DELIVERED (Đã giao thành công)
        // SHIPPING -> RETURN_REQUESTED (Giao không thành công / Hoàn hàng)
        if (newStatus == OrderStatus.SHIPPING && currentStatus != OrderStatus.PICKED_UP) {
            throw new IllegalArgumentException("Chỉ đơn 'Đã lấy hàng' mới có thể chuyển sang 'Đang giao'!");
        } else if (newStatus == OrderStatus.DELIVERED && currentStatus != OrderStatus.SHIPPING) {
            throw new IllegalArgumentException("Chỉ đơn 'Đang giao' mới có thể chuyển sang 'Đã giao'!");
        }

        order.setStatus(newStatus);
        if (newStatus == OrderStatus.DELIVERED && order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
        }
        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(newStatus)
                .note(note != null && !note.trim().isEmpty() ? note.trim() : "Shipper cập nhật trạng thái đơn: " + newStatus.getLabel())
                .updatedBy(shipper)
                .build();
        orderStatusHistoryRepository.save(history);

        notificationService.sendOrderNotification(order, "Đơn hàng " + order.getOrderCode() + " đã được cập nhật: " + newStatus.getLabel());
    }

    private String generateOrderCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randPart = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + datePart + "-" + randPart;
    }
}
