package com.cts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.model.CitizenDocument;

public interface CitizenDocumentRepository extends JpaRepository<CitizenDocument, Long>{

	List<CitizenDocument> findByCitizenCitizenId(Long citizenId);
}
