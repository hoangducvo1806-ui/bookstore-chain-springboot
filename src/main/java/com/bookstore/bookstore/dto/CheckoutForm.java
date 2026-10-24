package com.bookstore.bookstore.dto;

import com.bookstore.bookstore.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutForm {

    private Long addressId;

    // Cho phép nhập địa chỉ mới ngay tại checkout nếu chưa có địa chỉ lưu sẵn
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;

    @NotNull(message = "Vui lòng chọn đơn vị vận chuyển")
    private Long carrierId;

    private String voucherCode;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod;

    private String note;
}
