package com.bookstore.bookstore.enums;

/**
 * Trạng thái đơn hàng – DÙNG CHUNG cho cả nhóm (TV1: lịch sử mua hàng, TV2: Vendor/Shipper xử lý đơn).
 * Không tự ý đổi tên/thứ tự khi chưa thống nhất với nhóm.
 */
public enum OrderStatus {
    NEW("Đơn mới"),
    CONFIRMED("Đã xác nhận"),
    PICKED_UP("Đã lấy hàng"),
    SHIPPING("Đang giao"),
    DELIVERED("Đã giao"),
    CANCELLED("Đã hủy"),
    RETURN_REQUESTED("Yêu cầu trả hàng"),
    RETURNED_REFUNDED("Trả hàng - Hoàn tiền");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
