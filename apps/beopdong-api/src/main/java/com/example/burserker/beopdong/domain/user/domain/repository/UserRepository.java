package com.example.burserker.beopdong.domain.user.domain.repository;

import com.example.burserker.beopdong.domain.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    Slice<User> findAllBy(Pageable pageable);

    List<User> findAllByNameContainingOrderByIdAsc(String name);
}