package com.example.burserker.beopdong.domain.purpose.domain.repository;

import com.example.burserker.beopdong.domain.purpose.domain.PurposeUsage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurposeUsageRepository extends JpaRepository<PurposeUsage, Long> {

    @Query("""
            SELECT COUNT(usage) > 0
            FROM PurposeUsage usage JOIN usage.logs visitor
            WHERE visitor.user.id = :userId
              AND usage.completed = false
              AND usage.endAt > :now
            """)
    boolean existsActiveUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("""
            SELECT COUNT(usage) > 0
            FROM PurposeUsage usage
            WHERE usage.purpose.id = :purposeId
              AND usage.seat = :seat
              AND usage.completed = false
              AND usage.endAt > :now
            """)
    boolean existsActiveSeat(@Param("purposeId") Long purposeId, @Param("seat") Integer seat, @Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"purpose", "logs"})
    @Query("SELECT usage FROM PurposeUsage usage WHERE usage.id = :id")
    Optional<PurposeUsage> findUsageWithLogs(@Param("id") Long id);

    @EntityGraph(attributePaths = {"purpose", "logs"})
    @Query("""
            SELECT usage
            FROM PurposeUsage usage
            WHERE usage.id = (
                SELECT MIN(active.id)
                FROM PurposeUsage active
                WHERE active.purpose.id = :purposeId
                  AND active.completed = false
                  AND active.endAt > :now
            )
            """)
    Optional<PurposeUsage> findActiveRoomUsage(@Param("purposeId") Long purposeId, @Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"purpose", "logs"})
    @Query("""
            SELECT usage
            FROM PurposeUsage usage
            WHERE usage.completed = false
              AND usage.endAt > :now
            """)
    List<PurposeUsage> findActiveUsages(@Param("now") LocalDateTime now);

    @Query("""
            SELECT usage.id
            FROM PurposeUsage usage
            WHERE usage.completed = false AND usage.endAt <= :now
            ORDER BY usage.id
            """)
    List<Long> findExpiredUsageIds(@Param("now") LocalDateTime now);
}
