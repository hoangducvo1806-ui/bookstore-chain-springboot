package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.CartShopGroupDto;
import com.bookstore.bookstore.dto.CheckoutForm;
import com.bookstore.bookstore.entity.Address;
import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.ShippingCarrier;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.PaymentMethod;
import com.bookstore.bookstore.repository.ShippingCarrierRepository;
import com.bookstore.bookstore.service.AddressService;
import com.bookstore.bookstore.service.CartService;
import com.bookstore.bookstore.service.OrderService;
import com.bookstore.bookstore.service.VnPayService;
import com.bookstore.bookstore.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final OrderService orderService;
    private final CartService cartService;
    private final AddressService addressService;
    private final ShippingCarrierRepository shippingCarrierRepository;
    private final VnPayService vnPayService;
    private final SecurityUtils securityUtils;

    @GetMapping("/checkout")
    public String checkoutPage(Model model, RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để tiến hành đặt hàng!");
            return "redirect:/login";
        }

        List<CartShopGroupDto> shopGroups = cartService.getCartGroupedByShop(user);
        if (shopGroups.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống! Vui lòng chọn sách trước.");
            return "redirect:/cart";
        }

        List<Address> addresses = addressService.getAddressesByUser(user);
        Address defaultAddress = addressService.getDefaultAddress(user);
        List<ShippingCarrier> carriers = shippingCarrierRepository.findByActiveTrue();
        BigDecimal subtotal = cartService.getCartSubtotal(user);

        CheckoutForm form = new CheckoutForm();
        if (defaultAddress != null) {
            form.setAddressId(defaultAddress.getId());
        }
        if (!carriers.isEmpty()) {
            form.setCarrierId(carriers.get(0).getId());
        }
        form.setPaymentMethod(PaymentMethod.COD);

        model.addAttribute("checkoutForm", form);
        model.addAttribute("shopGroups", shopGroups);
        model.addAttribute("addresses", addresses);
        model.addAttribute("defaultAddress", defaultAddress);
        model.addAttribute("carriers", carriers);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("paymentMethods", PaymentMethod.values());

        return "user/checkout";
    }

    @PostMapping("/checkout")
    public String handleCheckout(@Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                                 BindingResult bindingResult,
                                 HttpServletRequest request,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("shopGroups", cartService.getCartGroupedByShop(user));
            model.addAttribute("addresses", addressService.getAddressesByUser(user));
            model.addAttribute("carriers", shippingCarrierRepository.findByActiveTrue());
            model.addAttribute("subtotal", cartService.getCartSubtotal(user));
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "user/checkout";
        }

        try {
            List<Order> orders = orderService.checkout(user, form);
            if (orders.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi tạo đơn hàng!");
                return "redirect:/cart";
            }

            Order primaryOrder = orders.get(0);

            // Xử lý thanh toán VNPAY Sandbox
            if (form.getPaymentMethod() == PaymentMethod.VNPAY) {
                String clientIp = request.getRemoteAddr();
                String scheme = request.getScheme();
                String serverName = request.getServerName();
                int serverPort = request.getServerPort();
                String baseUrl = scheme + "://" + serverName + (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort);
                String returnUrl = baseUrl + "/payment/vnpay-return";

                String vnpayUrl = vnPayService.createPaymentUrl(primaryOrder, clientIp, returnUrl);
                return "redirect:" + vnpayUrl;
            }

            // Thanh toán COD
            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công!");
            return "redirect:/checkout/success?orderCode=" + primaryOrder.getOrderCode();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/checkout";
        }
    }

    // Callback xử lý kết quả VNPAY
    @GetMapping("/payment/vnpay-return")
    public String vnpayReturn(@RequestParam Map<String, String> queryParams,
                              RedirectAttributes redirectAttributes) {
        boolean isValidHash = vnPayService.verifyCallback(queryParams);
        String orderInfo = queryParams.get("vnp_TxnRef");
        String orderCode = orderInfo != null && orderInfo.contains("_") ? orderInfo.split("_")[0] : orderInfo;

        if (!isValidHash) {
            log.error("VNPAY callback: Chữ ký không hợp lệ!");
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi bảo mật: Chữ ký số giao dịch VNPAY không hợp lệ!");
            return "redirect:/user/orders";
        }

        boolean isSuccess = vnPayService.isPaymentSuccess(queryParams);
        if (isSuccess) {
            String transactionId = vnPayService.getTransactionId(queryParams);
            orderService.updatePaymentSuccess(orderCode, transactionId, "VNPAY Transaction: " + transactionId);
            redirectAttributes.addFlashAttribute("successMessage", "Thanh toán qua VNPAY thành công!");
            return "redirect:/checkout/success?orderCode=" + orderCode;
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Giao dịch thanh toán VNPAY không thành công hoặc đã bị hủy!");
            return "redirect:/user/orders";
        }
    }

    @GetMapping("/checkout/success")
    public String checkoutSuccess(@RequestParam("orderCode") String orderCode, Model model) {
        User user = securityUtils.getCurrentUser().orElse(null);
        Order order = null;
        if (user != null) {
            try {
                order = orderService.getOrderByCode(orderCode, user);
            } catch (Exception ignored) {}
        }
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("order", order);
        return "user/order-success";
    }
}
