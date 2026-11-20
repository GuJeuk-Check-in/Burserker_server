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
    private Gender gender;

    private Integer age;

    private String phone;

    private String visitPurpose;

    private String note;

    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false)
    private LocalDateTime visitAt;

    public static Log createAdult(LocalDateTime visitAt) {
        return Log.builder().visitAt(visitAt).build();
    }

    public static Log createForOrgan(String name, int age, Gender gender, String phone,
                                     LocalDateTime visitAt, String visitPurpose, String note) {
        Log log = new Log();
        log.update(name, age, gender, phone, visitAt, visitPurpose, note);
        return log;
    }

    public void update(String name, Integer age, Gender gender, String phone,
                       LocalDateTime visitAt, String visitPurpose, String note) {
        boolean adult = age == null || age >= 20;
        this.name = adult ? null : blankToNull(name);
        this.age = age;
        this.gender = gender;
        this.phone = adult ? null : phone;
        this.visitAt = visitAt;
        this.visitPurpose = adult ? blankToNull(visitPurpose) : null;
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
