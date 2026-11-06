package com.example.burserker.beopdong.domain.log.domain.repository;

import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
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

    // 인터넷 좌석 사용 중인지
    boolean existsByPurposeIdAndSeatAndPurposeStateAndEndAtAfter(
            Long purposeId,
            Integer seat,
            PurposeState purposeState,
            LocalDateTime now
    );

    // 탁구, 노래방 사용 중인지
    boolean existsByPurposeIdAndPurposeStateAndEndAtAfter(
            Long purposeId,
            PurposeState purposeState,
            LocalDateTime now
    );

    // 유저가 오늘 이미 시설 사용 했는지
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

}