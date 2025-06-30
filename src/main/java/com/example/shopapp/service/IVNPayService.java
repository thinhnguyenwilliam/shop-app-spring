package com.example.shopapp.service;

import com.example.shopapp.dtos.request.payment.PaymentDTO;
import com.example.shopapp.dtos.request.payment.PaymentQueryDTO;
import com.example.shopapp.dtos.request.payment.PaymentRefundDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public interface IVNPayService {
    String createPaymentUrl(PaymentDTO paymentRequest, HttpServletRequest request);
    String queryTransaction(PaymentQueryDTO paymentQueryDTO, HttpServletRequest request) throws IOException;
    String refundTransaction(PaymentRefundDTO refundDTO) throws IOException;
}

