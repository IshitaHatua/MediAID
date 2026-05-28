package com.cts.enrollment_service.repository;

import com.cts.enrollment_service.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByCitizenId(Long citizenId);

    Boolean existsByCitizenIdAndSchemeId(Long citizenId, Long schemeId);

    Boolean existsByCitizenIdAndSchemeIdAndStatus(Long citizenId, Long schemeId, Enrollment.EnrollmentStatus status);

    Optional<Enrollment> findByCitizenIdAndSchemeId(Long citizenId, Long schemeId);
}
