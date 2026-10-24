package com.bookstore.bookstore.service;

import com.bookstore.bookstore.entity.Voucher;

import java.math.BigDecimal;

public interface VoucherService {

    Voucher validateAndGetVoucher(String code, BigDecimal orderSubtotal);

    BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderSubtotal);

    void recordVoucherUsage(Voucher voucher);
}
