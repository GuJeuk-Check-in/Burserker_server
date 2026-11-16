package com.example.burserker.beopdong.domain.log.domain.repository;

import com.example.burserker.beopdong.domain.log.domain.PurposeUsage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurposeUsageRepository extends JpaRepository<PurposeUsage, Long> {

    @Query("""
            SELECT COUNT(member) > 0
            FROM PurposeUsageMember member
            WHERE member.log.user.id = :userId
              AND member.purposeUsage.completed = false
              AND member.purposeUsage.endAt > :now
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

    @EntityGraph(attributePaths = {"purpose", "members", "members.log"})
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

    @EntityGraph(attributePaths = {"purpose", "members", "members.log"})
    @Query("""
            SELECT usage
            FROM PurposeUsage usage
            WHERE usage.completed = false
              AND usage.endAt > :now
            """)
    List<PurposeUsage> findActiveUsages(@Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"purpose", "members", "members.log"})
    @Query("""
            SELECT usage
            FROM PurposeUsage usage
            WHERE usage.completed = false
              AND usage.endAt <= :now
            """)
    List<PurposeUsage> findExpiredUsages(@Param("now") LocalDateTime now);
}
