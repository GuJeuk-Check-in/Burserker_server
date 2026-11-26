package com.example.burserker.gujeuk.domain.purpose.domain.repository;

import com.example.burserker.gujeuk.domain.purpose.domain.SubPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubPurposeRepository extends JpaRepository<SubPurpose, Long> {

    boolean existsByPurposeIdAndSubPurposeName(Long purposeId, String subPurposeName);

    boolean existsByPurposeId(Long purposeId);

    void deleteAllByPurposeId(Long purposeId);

    @Query("select coalesce(max(s.subPurposeIndex), 0) from SubPurpose s where s.purpose.id = :purposeId")
    int findMaxSubPurposeIndexByPurposeId(@Param("purposeId") Long purposeId);
}
