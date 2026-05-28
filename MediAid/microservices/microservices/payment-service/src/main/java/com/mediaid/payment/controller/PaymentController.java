package com.mediaid.payment.controller;

import com.mediaid.payment.api.APIResponse;
import com.mediaid.payment.dto.request.PaymentRequestDTO;
import com.mediaid.payment.dto.response.PaymentResponseDTO;
import com.mediaid.payment.security.CurrentUserUtil;
import com.mediaid.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentUserUtil currentUserUtil;

    @PreAuthorize("hasRole('OFFICER')")
    @PostMapping
    public ResponseEntity<APIResponse<PaymentResponseDTO>> createPayment(
            @RequestBody @Valid PaymentRequestDTO requestDTO) {
        PaymentResponseDTO payment = paymentService.createPayment(requestDTO);
        return ResponseEntity.ok(APIResponse.<PaymentResponseDTO>builder()
                .status("SUCCESS").message("Created Payment").data(payment).build());
    }

    // CITIZEN views their own payments — citizenId sourced from JWT, never hardcoded.
    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<PaymentResponseDTO>>> getMyPayments() {
        Long citizenId = currentUserUtil.getUserId();
        List<PaymentResponseDTO> payments = paymentService.getMyPayments(citizenId);
        return ResponseEntity.ok(APIResponse.<List<PaymentResponseDTO>>builder()
                .status("SUCCESS").message("Fetched My Payments").data(payments).build());
    }

    @PreAuthorize("hasRole('OFFICER')")
    @GetMapping("/all")
    public ResponseEntity<APIResponse<List<PaymentResponseDTO>>> getAllPayments() {
        List<PaymentResponseDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(APIResponse.<List<PaymentResponseDTO>>builder()
                .status("SUCCESS").message("Fetched All Payments").data(payments).build());
    }

    @PreAuthorize("hasAnyRole('CITIZEN', 'OFFICER')")
    @GetMapping("/disbursement/{disbursementId}")
    public ResponseEntity<APIResponse<PaymentResponseDTO>> getPaymentByDisbursement(
            @PathVariable Long disbursementId) {
        PaymentResponseDTO payment = paymentService.getPaymentByDisbursement(disbursementId);
        return ResponseEntity.ok(APIResponse.<PaymentResponseDTO>builder()
                .status("SUCCESS").message("Fetched Payment").data(payment).build());
    }

    @PreAuthorize("hasAnyRole('CITIZEN', 'OFFICER')")
    @GetMapping("/{paymentId}")
    public ResponseEntity<APIResponse<PaymentResponseDTO>> getPaymentById(
            @PathVariable Long paymentId) {
        PaymentResponseDTO payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(APIResponse.<PaymentResponseDTO>builder()
                .status("SUCCESS").message("Fetched Payment").data(payment).build());
    }
}
