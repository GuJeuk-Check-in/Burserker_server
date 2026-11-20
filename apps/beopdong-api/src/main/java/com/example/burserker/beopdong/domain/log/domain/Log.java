package com.example.burserker.beopdong.domain.log.domain;

import com.example.burserker.beopdong.domain.user.domain.User;
import com.example.burserker.beopdong.domain.user.domain.enums.Gender;
import com.example.burserker.beopdong.global.entity.BaseIdEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Log extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private int age;

    private String phone;

    @Column(columnDefinition = "TEXT")
    private String visitPurpose;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false)
    private LocalDateTime visitAt;

    public static Log createForOrgan(String name, int age, Gender gender, String phone,
                                     LocalDateTime visitAt, String visitPurpose, String note) {
        Log log = new Log();
        log.update(name, age, gender, phone, visitAt, visitPurpose, note);
        return log;
    }

    public void update(String name, int age, Gender gender, String phone,
                       LocalDateTime visitAt, String visitPurpose, String note) {
        this.name = blankToNull(name);
        this.age = age;
        this.gender = gender;
        this.phone = age < 20 ? phone : null;
        this.visitAt = visitAt;
        this.visitPurpose = age >= 20 ? blankToNull(visitPurpose) : null;
        this.note = blankToNull(note);
    }

    public void markDeleted() {
        this.deleted = true;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public long getOrganRemainTime(LocalDateTime now) {
        long elapsedMinutes = Duration.between(visitAt, now).toMinutes();
        return Math.max(0, 180 - elapsedMinutes);
    }

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
