package com.example.burserker.beopdong.domain.log.domain.repository;

import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurposeLogRepository extends JpaRepository<PurposeLog, Long> {

    @Query("""
        SELECT pl
        FROM PurposeLog pl
        WHERE pl.purposeState = :state
          AND (:purposeId IS NULL OR pl.purpose.id = :purposeId)
        ORDER BY pl.startAt DESC, pl.id DESC
        """)
    Page<PurposeLog> findCompletedUsages(@Param("state") PurposeState state,
                                       @Param("purposeId") Long purposeId, Pageable pageable);
}
