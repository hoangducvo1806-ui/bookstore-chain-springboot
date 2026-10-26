package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.OrderStatus;
import com.bookstore.bookstore.enums.PaymentStatus;
import com.bookstore.bookstore.repository.OrderRepository;
import com.bookstore.bookstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/shipper")
@RequiredArgsConstructor
public class ShipperController {

    private final OrderRepository orderRepository;
    private final SecurityUtils securityUtils;

    @GetMapping({"", "/", "/orders"})
    public String orders(Model model) {
        User shipper = securityUtils.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập với tài khoản Shipper."));

        List<Order> availableOrders = orderRepository.findAvailableForShipper(
                shipper,
                List.of(OrderStatus.CONFIRMED, OrderStatus.PICKED_UP, OrderStatus.SHIPPING)
        );

        List<Order> myDeliveries = orderRepository.findByShipperOrderByCreatedAtDesc(shipper);
        long deliveredCount = myDeliveries.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count();

        model.addAttribute("orders", availableOrders);
        model.addAttribute("myDeliveries", myDeliveries);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("shipper", shipper);

        return "shipper/orders";
    }

    @PostMapping("/orders/{id}/deliver")
    public String startDelivery(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User shipper = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));

            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));

            order.setShipper(shipper);
            order.setStatus(OrderStatus.SHIPPING);
            orderRepository.save(order);

            redirectAttributes.addFlashAttribute("successMessage", "Bạn đã nhận giao đơn " + order.getOrderCode() + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/shipper/orders";
    }

    @PostMapping("/orders/{id}/delivered")
    public String completeDelivery(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User shipper = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));

            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));

            order.setStatus(OrderStatus.DELIVERED);
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            orderRepository.save(order);

            redirectAttributes.addFlashAttribute("successMessage", "Giao thành công đơn " + order.getOrderCode() + "! Tiền hàng đã được ghi nhận.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/shipper/orders";
    }

    @PostMapping("/orders/{id}/failed")
    public String failDelivery(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Khách không nghe máy / hẹn lại sau") String reason,
            RedirectAttributes redirectAttributes) {
        try {
            User shipper = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));

            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));

            order.setStatus(OrderStatus.CANCELLED);
            order.setNote((order.getNote() != null ? order.getNote() + " | " : "") + "Lý do giao thất bại: " + reason);
            orderRepository.save(order);

            redirectAttributes.addFlashAttribute("successMessage", "Đã ghi nhận giao thất bại cho đơn " + order.getOrderCode());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/shipper/orders";
    }
}
