package com.mediaid.payment.repository;

import com.mediaid.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByDisbursementId(Long disbursementId);
    Optional<Payment> findByPaymentId(Long paymentId);
    List<Payment> findByCitizenId(Long citizenId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.disbursementId = :disbursementId AND LOWER(p.status) = 'completed'")
    BigDecimal sumCompletedPaymentsByDisbursementId(@Param("disbursementId") Long disbursementId);
}
