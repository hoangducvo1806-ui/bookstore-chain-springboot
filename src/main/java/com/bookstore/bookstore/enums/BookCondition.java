package com.bookstore.bookstore.enums;

/**
 * Tình trạng sách cũ (đặc thù của đề tài chuỗi cửa hàng sách cũ)
 */
public enum BookCondition {
    LIKE_NEW("Như mới (99%)"),
    VERY_GOOD("Rất tốt (90%)"),
    GOOD("Tốt (75-85%)"),
    ACCEPTABLE("Chấp nhận được (60-70%)"),
    OLD("Cũ / Sưu tầm");

    private final String label;

    BookCondition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
