package com.example.burserker.beopdong.domain.log.domain.repository;

import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurposeLogRepository
        extends JpaRepository<PurposeLog, Long> {

    Optional<PurposeLog> findByLogIdAndPurposeId(
            Long logId,
            Long purposeId
    );

    boolean existsByLogUserIdAndPurposeStateAndEndAtAfter(
            Long userId,
            PurposeState purposeState,
            LocalDateTime now
    );

    // 인터넷 좌석 사용 중 여부
    boolean existsByPurposeIdAndSeatAndPurposeStateAndEndAtAfter(
            Long purposeId,
            Integer seat,
            PurposeState purposeState,
            LocalDateTime now
    );

    // 탁구/노래방 사용 중 여부
    boolean existsByPurposeIdAndPurposeStateAndEndAtAfter(
            Long purposeId,
            PurposeState purposeState,
            LocalDateTime now
    );

    // 유저 해당 시설 당일 사용 여부
    @Query("""
            SELECT COUNT(pl) > 0
            FROM PurposeLog pl
            WHERE pl.log.user.id = :userId
              AND pl.purpose.id = :purposeId
              AND pl.startAt >= :dayStart
              AND pl.startAt < :nextDayStart
            """)
    boolean existsDailyUsage(
            @Param("userId") Long userId,
            @Param("purposeId") Long purposeId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("nextDayStart") LocalDateTime nextDayStart
    );

    List<PurposeLog> findAllByPurposeStateAndEndAtLessThanEqual(
            PurposeState purposeState,
            LocalDateTime now
    );

    List<PurposeLog> findAllByPurposeStateAndEndAtAfter(
            PurposeState purposeState,
            LocalDateTime now
    );
}