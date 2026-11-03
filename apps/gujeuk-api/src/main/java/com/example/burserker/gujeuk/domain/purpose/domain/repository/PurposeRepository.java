package com.example.burserker.gujeuk.domain.purpose.domain.repository;

import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurposeRepository extends JpaRepository<Purpose, Long>, PurposeRepositoryCustom {

    Optional<Purpose> findByOrganIdAndPurposeName(Long organId, String purposeName);

    List<Purpose> findAllByOrganIdAndPurposeIndexGreaterThan(Long organId, int purposeIndex);

    List<Purpose> findAllByOrderByPurposeIndexAsc();

    int countByOrganIdAndReservationStatusTrue(Long organId);
}
