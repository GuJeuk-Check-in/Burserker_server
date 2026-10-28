package com.example.burserker.beopdong.domain.organ.domain.repository;

import com.example.burserker.beopdong.domain.organ.domain.Organ;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganRepository extends JpaRepository<Organ, Long> {

    Optional<Organ> findByOrganName(String organName);
}
