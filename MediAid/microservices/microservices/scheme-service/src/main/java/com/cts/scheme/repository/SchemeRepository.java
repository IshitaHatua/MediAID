package com.cts.scheme.repository;

import com.cts.scheme.model.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchemeRepository extends JpaRepository<Scheme,Long> {
}
