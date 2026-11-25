package com.example.burserker.beopdong.domain.log.domain.repository;

import com.example.burserker.beopdong.domain.log.domain.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LogRepository extends JpaRepository<Log, Long> {

    @Override
    @Query("SELECT log FROM Log log WHERE log.id = :id AND log.deleted = false")
    Optional<Log> findById(@Param("id") Long id);

    // 일반 성인 직접 방문은 인원 집계용으로 저장하고 방명록 목록에서는 제외한다.
    @Query("SELECT log FROM Log log WHERE log.deleted = false AND (log.user IS NOT NULL OR log.age IS NOT NULL) ORDER BY log.visitAt DESC, log.id DESC")
    List<Log> findLogs(Pageable pageable);

    @Query("SELECT log FROM Log log WHERE log.deleted = false AND (log.user IS NOT NULL OR log.age IS NOT NULL) ORDER BY log.visitAt DESC, log.id DESC")
    Page<Log> findOrganLogs(Pageable pageable);
}
