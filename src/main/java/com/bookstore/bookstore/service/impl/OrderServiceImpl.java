package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.dto.CheckoutForm;
import com.bookstore.bookstore.entity.*;
import com.bookstore.bookstore.enums.OrderStatus;
import com.bookstore.bookstore.enums.PaymentMethod;
import com.bookstore.bookstore.enums.PaymentStatus;
import com.bookstore.bookstore.repository.*;
import com.bookstore.bookstore.service.AddressService;
import com.bookstore.bookstore.service.CartService;
import com.bookstore.bookstore.service.OrderService;
import com.bookstore.bookstore.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final BookRepository bookRepository;
    private final ShippingCarrierRepository shippingCarrierRepository;
    private final CartService cartService;
    private final AddressService addressService;
    private final VoucherService voucherService;

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

    private String generateOrderCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randPart = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + datePart + "-" + randPart;
    }
}
