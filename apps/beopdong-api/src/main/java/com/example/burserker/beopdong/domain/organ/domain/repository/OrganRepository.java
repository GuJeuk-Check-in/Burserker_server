package com.example.burserker.beopdong.domain.organ.domain.repository;

import com.example.burserker.beopdong.domain.organ.domain.Organ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface OrganRepository extends JpaRepository<Organ, Long> {

    Optional<Organ> findByOrganName(String organName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Organ> findWithLockById(Long id);

    Optional<Organ> findFirstByOrderByIdAsc();
}
