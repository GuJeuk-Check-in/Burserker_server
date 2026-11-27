package com.example.burserker.gujeuk.domain.purpose.domain.repository;

import com.example.burserker.gujeuk.domain.purpose.domain.SubPurpose;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubPurposeRepository extends JpaRepository<SubPurpose, Long> {

    boolean existsByPurposeIdAndSubPurposeName(Long purposeId, String subPurposeName);

    // 잠금 읽기로 조회해 스냅샷과 관계없이 최신 커밋 상태를 본다
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SubPurpose> findWithLockById(Long id);

    // 잠금 읽기로 조회해 스냅샷과 관계없이 최신 커밋 상태로 이름 중복을 검사한다
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SubPurpose> findWithLockByPurposeIdAndSubPurposeNameAndIdNot(
            Long purposeId, String subPurposeName, Long id);

    boolean existsByPurposeId(Long purposeId);

    void deleteAllByPurposeId(Long purposeId);

    @Query("select coalesce(max(s.subPurposeIndex), 0) from SubPurpose s where s.purpose.id = :purposeId")
    int findMaxSubPurposeIndexByPurposeId(@Param("purposeId") Long purposeId);

    // 잠금 읽기로 같은 상위 방문목적의 하위 방문목적 전체를 최신 커밋 상태로 조회한다
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<SubPurpose> findAllWithLockByPurposeId(Long purposeId);

    @Query("select s from SubPurpose s join fetch s.purpose p where s.id = :id and p.organ.id = :organId")
    Optional<SubPurpose> findWithPurposeByIdAndOrganId(@Param("id") Long id, @Param("organId") Long organId);

    // 기관의 하위 방문목적을 상위 방문목적별 순서대로 조회한다
    @Query("select s from SubPurpose s join s.purpose p where p.organ.id = :organId "
            + "order by p.purposeIndex asc, p.id asc, s.subPurposeIndex asc")
    List<SubPurpose> findAllByOrganIdOrderByIndex(@Param("organId") Long organId);

    @Query("select s from SubPurpose s order by s.purpose.id asc, s.subPurposeIndex asc")
    List<SubPurpose> findAllOrderByPurposeIdAndSubPurposeIndex();
}
