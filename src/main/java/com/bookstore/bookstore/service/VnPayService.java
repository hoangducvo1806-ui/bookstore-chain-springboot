package com.bookstore.bookstore.service;

import com.bookstore.bookstore.entity.Order;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VnPayService {

    String createPaymentUrl(Order order, String clientIp, String returnUrl);

    boolean verifyCallback(Map<String, String> queryParams);

    String getTransactionId(Map<String, String> queryParams);

    boolean isPaymentSuccess(Map<String, String> queryParams);
}
