package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.entity.Voucher;
import com.bookstore.bookstore.repository.VoucherRepository;
import com.bookstore.bookstore.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    @Transactional(readOnly = true)
    public Voucher validateAndGetVoucher(String code, BigDecimal orderSubtotal) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        Voucher voucher = voucherRepository.findByCodeIgnoreCaseAndActiveTrue(code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá '" + code + "' không tồn tại hoặc đã hết hiệu lực!"));

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            throw new IllegalArgumentException("Mã giảm giá chưa đến ngày áp dụng!");
        }

        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn sử dụng!");
        }

        if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null 
                && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng!");
        }

        if (voucher.getMinOrderAmount() != null && orderSubtotal != null 
                && orderSubtotal.compareTo(voucher.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException("Đơn hàng tối thiểu phải từ " 
                    + String.format("%,.0f", voucher.getMinOrderAmount()) + " VNĐ để áp dụng mã này!");
        }

        return voucher;
    }

    @Override
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderSubtotal) {
        if (voucher == null || orderSubtotal == null || orderSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;
        if ("PERCENTAGE".equalsIgnoreCase(voucher.getDiscountType())) {
            // Giảm theo %
            discount = orderSubtotal.multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (voucher.getMaxDiscountAmount() != null && discount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discount = voucher.getMaxDiscountAmount();
            }
        } else if ("FIXED_AMOUNT".equalsIgnoreCase(voucher.getDiscountType())) {
            discount = voucher.getDiscountValue();
        } else if ("FREE_SHIPPING".equalsIgnoreCase(voucher.getDiscountType())) {
            discount = voucher.getDiscountValue() != null ? voucher.getDiscountValue() : BigDecimal.valueOf(30000);
        }

        if (discount.compareTo(orderSubtotal) > 0) {
            discount = orderSubtotal;
        }

        return discount;
    }

    @Override
    public void recordVoucherUsage(Voucher voucher) {
        if (voucher != null) {
            voucher.setUsedCount((voucher.getUsedCount() != null ? voucher.getUsedCount() : 0) + 1);
            voucherRepository.save(voucher);
        }
    }
}
