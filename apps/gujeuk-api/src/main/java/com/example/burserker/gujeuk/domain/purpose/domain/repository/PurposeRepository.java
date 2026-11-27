package com.example.burserker.gujeuk.domain.purpose.domain.repository;

import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurposeRepository extends JpaRepository<Purpose, Long>, PurposeRepositoryCustom {

    Optional<Purpose> findByOrganIdAndPurposeName(Long organId, String purposeName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Purpose> findWithLockByIdAndOrganId(Long id, Long organId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Purpose> findWithLockByOrganIdAndPurposeName(Long organId, String purposeName);

    // 하위 방문목적 ID로 상위 방문목적을 찾아 바로 잠근다 (기관 소유 확인 포함)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Purpose p where p.organ.id = :organId and p.id = "
            + "(select s.purpose.id from SubPurpose s where s.id = :subPurposeId)")
    Optional<Purpose> findWithLockBySubPurposeIdAndOrganId(
            @Param("subPurposeId") Long subPurposeId, @Param("organId") Long organId);

    Optional<Purpose> findByIdAndOrganId(Long id, Long organId);

    List<Purpose> findAllByOrganIdAndPurposeIndexGreaterThan(Long organId, int purposeIndex);

    List<Purpose> findAllByOrderByPurposeIndexAsc();

    int countByOrganIdAndReservationStatus(Long organId, ReservationStatus reservationStatus);
}
