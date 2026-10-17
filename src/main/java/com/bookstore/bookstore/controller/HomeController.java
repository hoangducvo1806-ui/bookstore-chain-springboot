package com.bookstore.bookstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Trang chủ tạm thời để kiểm tra project chạy được.
 * TODO (TV1): hiển thị sách đã bán > 10 cuốn, sắp xếp giảm dần theo số lượng bán.
 */
@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "guest/index";
    }
}
