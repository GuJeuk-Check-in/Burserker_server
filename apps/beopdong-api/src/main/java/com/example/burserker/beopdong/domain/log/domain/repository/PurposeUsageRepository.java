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

    boolean existsByPurposeIdAndSeatAndCompletedFalseAndEndAtAfter(Long purposeId, Integer seat, LocalDateTime now);

    @EntityGraph(attributePaths = {"purpose", "members", "members.log"})
    Optional<PurposeUsage> findFirstByPurposeIdAndCompletedFalseAndEndAtAfterOrderByIdAsc(Long purposeId, LocalDateTime now);

    @EntityGraph(attributePaths = {"purpose", "members", "members.log"})
    List<PurposeUsage> findAllByCompletedFalseAndEndAtAfter(LocalDateTime now);

    @EntityGraph(attributePaths = {"purpose", "members", "members.log"})
    List<PurposeUsage> findAllByCompletedFalseAndEndAtLessThanEqual(LocalDateTime now);
}
