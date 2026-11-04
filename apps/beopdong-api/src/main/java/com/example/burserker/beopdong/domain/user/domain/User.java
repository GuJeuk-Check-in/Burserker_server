package com.example.burserker.beopdong.domain.user.domain;

import com.example.burserker.beopdong.domain.user.domain.enums.Route;
import com.example.burserker.beopdong.global.entity.BaseIdEntity;
import com.example.burserker.beopdong.domain.user.domain.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseIdEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private LocalDate birthYMD;

    @Column(nullable = false)
    private int grade;

    @Column(nullable = false)
    private String school;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Route route;

    private String routeDetail;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private boolean privacyAgreed;

    @Column(nullable = false)
    private int visitCount;

    @Column(nullable = false)
    private LocalDate applyDate;

    @PrePersist
    private void setApplyDate() {
        this.applyDate = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"));
    }

    public void increaseVisitCount() {
        this.visitCount = this.visitCount + 1;
    }

    public void updateUser(
            String name,
            int grade,
            String school,
            Route route,
            String routeDetail,
            String address,
            Gender gender,
            String phone,
            LocalDate birthYMD,
            LocalDate applyDate
    ) {
        this.name = name;
        this.grade = grade;
        this.school = school;
        this.route = route;
        this.routeDetail = route == Route.OTHER ? routeDetail : null;
        this.address = address;
        this.gender = gender;
        this.phone = phone;
        this.birthYMD = birthYMD;
        this.applyDate = applyDate;
    }
}