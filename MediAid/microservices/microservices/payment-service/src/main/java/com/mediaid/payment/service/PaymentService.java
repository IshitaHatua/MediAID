package com.mediaid.payment.service;

import com.mediaid.payment.dto.request.PaymentRequestDTO;
import com.mediaid.payment.dto.response.PaymentResponseDTO;

import java.util.List;

public interface PaymentService {
    PaymentResponseDTO createPayment(PaymentRequestDTO requestDTO);
    PaymentResponseDTO getPaymentByDisbursement(Long disbursementId);
    PaymentResponseDTO getPaymentById(Long paymentId);
    List<PaymentResponseDTO> getMyPayments(Long citizenId);
    List<PaymentResponseDTO> getAllPayments();
}
