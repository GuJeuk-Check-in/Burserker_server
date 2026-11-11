package com.example.burserker.gujeuk.domain.purpose.domain.repository;

import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface PurposeRepository extends JpaRepository<Purpose, Long>, PurposeRepositoryCustom {

    Optional<Purpose> findByOrganIdAndPurposeName(Long organId, String purposeName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Purpose> findWithLockByIdAndOrganId(Long id, Long organId);

    Optional<Purpose> findByIdAndOrganId(Long id, Long organId);

    List<Purpose> findAllByOrganIdAndPurposeIndexGreaterThan(Long organId, int purposeIndex);

    List<Purpose> findAllByOrderByPurposeIndexAsc();

    int countByOrganIdAndReservationStatusTrue(Long organId);
}
