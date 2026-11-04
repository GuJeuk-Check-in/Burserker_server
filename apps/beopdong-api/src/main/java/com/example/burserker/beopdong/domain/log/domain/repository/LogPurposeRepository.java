package com.example.burserker.beopdong.domain.log.domain.repository;

import com.example.burserker.beopdong.domain.log.domain.LogPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogPurposeRepository extends JpaRepository<LogPurpose, Long> {
}