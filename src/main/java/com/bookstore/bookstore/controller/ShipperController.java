package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.OrderStatus;
import com.bookstore.bookstore.service.OrderService;
import com.bookstore.bookstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/shipper")
@RequiredArgsConstructor
@Slf4j
public class ShipperController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    @GetMapping({"", "/", "/orders"})
    public String ordersPage(
            @RequestParam(value = "tab", defaultValue = "available") String tab,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model,
            RedirectAttributes redirectAttributes) {

        User shipper = securityUtils.getCurrentUser().orElse(null);
        if (shipper == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập với tài khoản Shipper!");
            return "redirect:/login";
        }

        Page<Order> orderPage;
        if ("available".equalsIgnoreCase(tab)) {
            orderPage = orderService.getAvailableOrdersForShipper(page, size);
        } else {
            orderPage = orderService.getOrdersByShipperAndTab(shipper, tab, page, size);
        }

        model.addAttribute("shipper", shipper);
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentTab", tab);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalOrders", orderPage.getTotalElements());

        return "shipper/orders";
    }

    @PostMapping("/orders/{id}/claim")
    public String claimOrder(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        User shipper = securityUtils.getCurrentUser().orElseThrow();
        try {
            orderService.claimOrder(shipper, id);
            redirectAttributes.addFlashAttribute("successMessage", "Bạn đã nhận giao đơn hàng này thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/shipper/orders?tab=picked_up";
    }

    @PostMapping("/orders/{id}/status")
    public String updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") OrderStatus newStatus,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "redirectTab", defaultValue = "shipping") String redirectTab,
            RedirectAttributes redirectAttributes) {

        User shipper = securityUtils.getCurrentUser().orElseThrow();
        try {
            orderService.updateOrderStatusByShipper(shipper, id, newStatus, note);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Cập nhật trạng thái giao hàng thành công: " + newStatus.getLabel());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/shipper/orders?tab=" + redirectTab;
    }
}
