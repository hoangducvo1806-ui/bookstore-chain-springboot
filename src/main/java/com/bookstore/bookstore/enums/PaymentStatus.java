package com.bookstore.bookstore.enums;

public enum PaymentStatus {
    PENDING("Chờ thanh toán"),
    COMPLETED("Đã thanh toán"),
    FAILED("Thanh toán thất bại"),
    REFUNDED("Đã hoàn tiền");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
