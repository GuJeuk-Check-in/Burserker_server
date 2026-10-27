package com.example.burserker.gujeuk.domain.user.domain.repository;

import com.example.burserker.gujeuk.domain.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    Slice<User> findByResidenceAndOrganId(String residence, Long organId, Pageable pageable);

    Slice<User> findByOrganIdAndResidenceNotIn (Long organId, List<String> residences, Pageable pageable);

    Slice<User> findAllByOrganId(Pageable pageable, Long organId);

    long countByResidenceAndOrganId(String residence, Long organId);

    long countByOrganId(Long organId);

    long countByOrganIdAndResidenceNotIn(Long organId, List<String> residences);

    boolean existsByIdAndOrganId(Long id, Long organId);

    List<User> findAllByName(String name);

    List<User> findAllByOrganIdAndName(Long organId, String name);

    User findByOrganIdAndName(Long organId, String name);

    @Query("SELECT u FROM User u WHERE u.organ.id = :organId AND REPLACE(u.phone, '-', '') = :phone")
    User findByOrganIdAndPhoneWithoutHyphens(@Param("organId") Long organId, @Param("phone") String phone);

    List<User> findAllByOrganIdOrderByIdAsc(Long organId);

    Slice<User> findAllByNameContainingOrderByIdAsc(String name, Pageable pageable);

    long countByNameContaining(String name);
}
