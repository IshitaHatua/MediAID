package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.model.Citizen;

public interface CitizenRepository extends JpaRepository<Citizen, Long>{

}
