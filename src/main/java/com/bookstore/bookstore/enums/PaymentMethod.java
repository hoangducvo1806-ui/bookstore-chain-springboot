package com.bookstore.bookstore.enums;

public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng"),
    VNPAY("Ví / Cổng thanh toán VNPAY"),
    MOMO("Ví điện tử MOMO");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
