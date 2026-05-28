package com.mediaid.payment.mapper;

import com.mediaid.payment.dto.request.PaymentRequestDTO;
import com.mediaid.payment.dto.response.PaymentResponseDTO;
import com.mediaid.payment.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(PaymentRequestDTO dto) {
        if (dto == null) return null;
        Payment p = new Payment();
        p.setDate(dto.getDate());
        p.setMethod(dto.getMethod());
        p.setStatus(dto.getStatus());
        p.setAmount(dto.getAmount());
        return p;
    }

    public PaymentResponseDTO toDto(Payment payment) {
        if (payment == null) return null;
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setDate(payment.getDate());
        dto.setMethod(payment.getMethod());
        dto.setStatus(payment.getStatus());
        dto.setAmount(payment.getAmount());
        dto.setDisbursementId(payment.getDisbursementId());
        dto.setCitizenId(payment.getCitizenId());
        return dto;
    }
}
