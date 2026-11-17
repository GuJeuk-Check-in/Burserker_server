package com.example.burserker.beopdong.domain.log.domain.repository;

import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PurposeLogRepository extends JpaRepository<PurposeLog, Long> {

    @Query("""
        SELECT pl
        FROM PurposeLog pl
        LEFT JOIN pl.purposeUsage original
        WHERE (pl.purposeState = :completedState
               OR (pl.purposeState = :pendingState AND original.endAt <= :now))
          AND (:purposeId IS NULL OR pl.purpose.id = :purposeId)
        ORDER BY pl.startAt DESC, pl.id DESC
        """)
    Page<PurposeLog> findCompletedUsages(@Param("completedState") PurposeState completedState,
                                       @Param("pendingState") PurposeState pendingState, @Param("purposeId") Long purposeId,
                                       @Param("now") LocalDateTime now, Pageable pageable);

    Optional<PurposeLog> findByPurposeUsageIdAndPurposeState(Long usageId, PurposeState state);
}
