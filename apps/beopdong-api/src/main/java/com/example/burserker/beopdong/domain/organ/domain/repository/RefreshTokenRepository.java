package com.example.burserker.beopdong.domain.organ.domain.repository;

import com.example.burserker.beopdong.domain.organ.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByOrganName(String organName);
}
