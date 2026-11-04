package com.example.burserker.beopdong.domain.log.domain;

import com.example.burserker.beopdong.domain.user.domain.User;
import com.example.burserker.beopdong.domain.user.domain.enums.Gender;
import com.example.burserker.beopdong.global.entity.BaseIdEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Log extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private LocalDateTime visitAt;

    public static Log create(User user, LocalDateTime visitAt) {
        int age = visitAt.getYear() - user.getBirthYMD().getYear() + 1;

        return Log.builder()
                .user(user)
                .name(user.getName())
                .gender(user.getGender())
                .age(age)
                .phone(user.getPhone())
                .visitAt(visitAt)
                .build();
    }
}