package com.example.burserker.beopdong.domain.log.domain.repository;

import com.example.burserker.beopdong.domain.log.domain.VisitPurpose;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisitPurposeRepository extends JpaRepository<VisitPurpose, Long> {

    boolean existsByLogIdAndPurposeId(Long logId, Long purposeId);

    Optional<VisitPurpose> findByLogIdAndPurposePurposeName(Long logId, String purposeName);

    @EntityGraph(attributePaths = "purpose")
    List<VisitPurpose> findAllByLogId(Long logId);

    @EntityGraph(attributePaths = "purpose")
    List<VisitPurpose> findAllByLogIdIn(List<Long> logIds);
}
