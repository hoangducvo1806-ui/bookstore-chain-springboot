package com.bookstore.bookstore.enums;

/**
 * 6 role của hệ thống (Guest = người chưa đăng nhập nên không cần lưu DB).
 * Spring Security sẽ dùng dạng "ROLE_" + name(), ví dụ ROLE_ADMIN.
 */
public enum RoleName {
    USER,
    VENDOR,
    SHIPPER,
    MANAGER,
    ADMIN
}
