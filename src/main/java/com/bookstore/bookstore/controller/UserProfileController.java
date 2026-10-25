package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.AddressDto;
import com.bookstore.bookstore.dto.UserProfileDto;
import com.bookstore.bookstore.entity.Address;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.service.AddressService;
import com.bookstore.bookstore.service.UserService;
import com.bookstore.bookstore.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final AddressService addressService;
    private final SecurityUtils securityUtils;

    @GetMapping("/profile")
    public String profilePage(Model model, RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        UserProfileDto profileDto = UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .build();

        List<Address> addresses = addressService.getAddressesByUser(user);

        model.addAttribute("profileDto", profileDto);
        model.addAttribute("addresses", addresses);
        if (!model.containsAttribute("newAddress")) {
            model.addAttribute("newAddress", new AddressDto());
        }

        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("profileDto") UserProfileDto profileDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) return "redirect:/login";

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thông tin nhập không hợp lệ!");
            return "redirect:/user/profile";
        }

        try {
            userService.updateProfile(user, profileDto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/user/profile";
    }

    @PostMapping("/profile/avatar")
    public String updateAvatar(@RequestParam("avatarFile") MultipartFile avatarFile,
                               RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) return "redirect:/login";

        try {
            userService.updateAvatar(user, avatarFile);
            redirectAttributes.addFlashAttribute("successMessage", "Thay đổi ảnh đại diện thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/user/profile";
    }

    // CRUD Địa chỉ nhận hàng
    @PostMapping("/addresses/add")
    public String addAddress(@Valid @ModelAttribute("newAddress") AddressDto addressDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) return "redirect:/login";

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu địa chỉ không hợp lệ: " 
                    + bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/user/profile";
        }

        try {
            addressService.createAddress(user, addressDto);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm địa chỉ nhận hàng mới!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/user/profile";
    }

    @PostMapping("/addresses/{id}/edit")
    public String editAddress(@PathVariable("id") Long id,
                              @Valid @ModelAttribute AddressDto addressDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) return "redirect:/login";

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thông tin địa chỉ không hợp lệ!");
            return "redirect:/user/profile";
        }

        try {
            addressService.updateAddress(user, id, addressDto);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật địa chỉ!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/user/profile";
    }

    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(@PathVariable("id") Long id,
                                RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) return "redirect:/login";

        try {
            addressService.deleteAddress(user, id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa địa chỉ thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/user/profile";
    }

    @PostMapping("/addresses/{id}/default")
    public String setDefaultAddress(@PathVariable("id") Long id,
                                    RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) return "redirect:/login";

        try {
            addressService.setDefaultAddress(user, id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đặt làm địa chỉ mặc định!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/user/profile";
    }
}
