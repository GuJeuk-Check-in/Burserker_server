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

    // 하위 방문목적 ID로 상위 방문목적을 찾아 잠근다 (기관 소유 확인 포함)
    // 서브쿼리는 잠금 없는 일반 읽기라 잠금 대기 전에 스냅샷이 만들어질 수 있다.
    // 따라서 이 조회 뒤의 읽기는 최신 상태가 보장되지 않으므로, 필요한 읽기는 잠금 읽기로 해야 한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Purpose p where p.organ.id = :organId and p.id = "
            + "(select s.purpose.id from SubPurpose s where s.id = :subPurposeId)")
    Optional<Purpose> findWithLockBySubPurposeIdAndOrganId(
            @Param("subPurposeId") Long subPurposeId, @Param("organId") Long organId);

    Optional<Purpose> findByIdAndOrganId(Long id, Long organId);

    List<Purpose> findAllByOrganIdAndPurposeIndexGreaterThan(Long organId, int purposeIndex);

    List<Purpose> findAllByOrderByPurposeIndexAsc();

    List<Purpose> findAllByOrderByPurposeIndexAscIdAsc();

    int countByOrganIdAndReservationStatus(Long organId, ReservationStatus reservationStatus);
}
