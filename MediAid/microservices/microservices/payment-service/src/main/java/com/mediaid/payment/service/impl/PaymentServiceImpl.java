package com.mediaid.payment.service.impl;

import com.mediaid.payment.client.AuditManagementFeignClient;
import com.mediaid.payment.client.DisbursementClient;
import com.mediaid.payment.dto.AuditManagementLogRequest;
import com.mediaid.payment.dto.request.PaymentRequestDTO;
import com.mediaid.payment.dto.response.DisbursementClientResponseDTO;
import com.mediaid.payment.dto.response.PaymentResponseDTO;
import com.mediaid.payment.exception.BadRequestException;
import com.mediaid.payment.exception.ResourceNotFoundException;
import com.mediaid.payment.mapper.PaymentMapper;
import com.mediaid.payment.model.Payment;
import com.mediaid.payment.repository.PaymentRepository;
import com.mediaid.payment.service.PaymentService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final DisbursementClient disbursementClient;

    // ── New: audit integration ────────────────────────────────────────────────
    private final AuditManagementFeignClient auditManagementFeignClient;

    @Override
    @CircuitBreaker(name = "disbursementService", fallbackMethod = "disbursementFallback")
    public PaymentResponseDTO createPayment(PaymentRequestDTO requestDTO) {
        Long disbursementId = requestDTO.getDisbursementId();

        DisbursementClientResponseDTO clientResponse =
                disbursementClient.getDisbursementById(disbursementId);

        if (clientResponse == null || clientResponse.getData() == null) {
            throw new ResourceNotFoundException("Disbursement not found with id: " + disbursementId);
        }

        Payment p = mapper.toEntity(requestDTO);
        p.setDisbursementId(disbursementId);
        p.setCitizenId(clientResponse.getData().getCitizenId());
        p = repository.save(p);

        // Log payment creation to audit-management-service (best-effort).
        writeAuditLog(clientResponse.getData().getCitizenId(), "PAYMENT_CREATED",
                "PAYMENT:" + p.getPaymentId(),
                "DisbursementId=" + disbursementId
                        + ", Amount=" + requestDTO.getAmount()
                        + ", CitizenId=" + clientResponse.getData().getCitizenId());

        // Auto-complete disbursement when total Completed payments reach the disbursement amount.
        boolean completed = autoCompleteDisbursement(disbursementId,
                clientResponse.getData().getAmount(),
                clientResponse.getData().getStatus());

        // If auto-completion fired, log that event too.
        if (completed) {
            writeAuditLog(clientResponse.getData().getCitizenId(),
                    "PAYMENT_COMPLETED_DISBURSEMENT",
                    "DISBURSEMENT:" + disbursementId,
                    "All payments received — disbursement auto-marked COMPLETED");
        }

        return mapper.toDto(p);
    }

    @Override
    public PaymentResponseDTO getPaymentByDisbursement(Long disbursementId) {
        return repository.findByDisbursementId(disbursementId)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for disbursement id: " + disbursementId));
    }

    @Override
    public PaymentResponseDTO getPaymentById(Long paymentId) {
        return repository.findByPaymentId(paymentId)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with id: " + paymentId));
    }

    @Override
    public List<PaymentResponseDTO> getMyPayments(Long citizenId) {
        return repository.findByCitizenId(citizenId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<PaymentResponseDTO> getAllPayments() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Returns true if the disbursement was auto-completed, false otherwise.
     */
    private boolean autoCompleteDisbursement(Long disbursementId,
                                              BigDecimal disbursementAmount,
                                              String currentStatus) {
        if (disbursementAmount == null) return false;

        BigDecimal totalCompleted = repository.sumCompletedPaymentsByDisbursementId(disbursementId);
        if (totalCompleted == null) totalCompleted = BigDecimal.ZERO;

        if (totalCompleted.compareTo(disbursementAmount) >= 0
                && !"COMPLETED".equalsIgnoreCase(currentStatus)) {
            disbursementClient.updateDisbursementStatus(disbursementId, "COMPLETED");
            return true;
        }
        return false;
    }

    private void writeAuditLog(Long userId, String action, String resource, String details) {
        try {
            auditManagementFeignClient.log(AuditManagementLogRequest.builder()
                    .userId(userId)
                    .action(action)
                    .resource(resource)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception ex) {
            log.error("[PaymentService] Unexpected error writing to audit-management-service: {}", ex.getMessage());
        }
    }

    public PaymentResponseDTO disbursementFallback(PaymentRequestDTO requestDTO, Throwable t) {
        log.error("[PaymentService] disbursementFallback fired for disbursementId={} cause={} message={}",
                requestDTO != null ? requestDTO.getDisbursementId() : "null",
                t.getClass().getSimpleName(), t.getMessage());
        if (t instanceof ResourceNotFoundException rne) throw rne;
        if (t instanceof BadRequestException bre) throw bre;
        if (t instanceof FeignException fe) {
            if (fe.status() == 404) throw new ResourceNotFoundException(
                    "Disbursement not found with id: " + requestDTO.getDisbursementId());
            if (fe.status() == 400) throw new BadRequestException(
                    "Bad request to Disbursement Service");
            if (fe.status() == 401 || fe.status() == 403)
                throw new RuntimeException("Payment service is not authorised to contact Disbursement Service. "
                        + "Check that the API gateway is injecting X-User-Id / X-User-Role headers.");
        }
        throw new RuntimeException("Disbursement Service is currently unavailable. Please try again later.");
    }
}
