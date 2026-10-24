package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.OrderStatus;
import com.bookstore.bookstore.service.OrderService;
import com.bookstore.bookstore.service.ReviewService;
import com.bookstore.bookstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user/orders")
@RequiredArgsConstructor
public class UserOrderController {

    private final OrderService orderService;
    private final ReviewService reviewService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String listOrders(@RequestParam(value = "tab", defaultValue = "all") String tab,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        List<Order> orders = orderService.getOrdersByUserAndTab(user, tab);

        model.addAttribute("orders", orders);
        model.addAttribute("currentTab", tab);

        return "user/orders";
    }

    @GetMapping("/{orderCode}")
    public String orderDetail(@PathVariable("orderCode") String orderCode,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            Order order = orderService.getOrderByCode(orderCode, user);
            model.addAttribute("order", order);
            model.addAttribute("canCancel", order.getStatus() == OrderStatus.NEW);
            model.addAttribute("canReturn", order.getStatus() == OrderStatus.DELIVERED);
            return "user/order-detail";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/user/orders";
        }
    }

    @PostMapping("/{orderCode}/cancel")
    public String cancelOrder(@PathVariable("orderCode") String orderCode,
                              @RequestParam(value = "reason", required = false) String reason,
                              RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            orderService.cancelOrder(orderCode, user, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng " + orderCode + " thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/user/orders/" + orderCode;
    }

    @PostMapping("/{orderCode}/return")
    public String requestReturnOrder(@PathVariable("orderCode") String orderCode,
                                     @RequestParam(value = "reason", required = false) String reason,
                                     RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            orderService.requestReturnOrder(orderCode, user, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi yêu cầu trả hàng / hoàn tiền cho đơn " + orderCode + "!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/user/orders/" + orderCode;
    }
}
