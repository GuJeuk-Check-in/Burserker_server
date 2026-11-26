package com.example.burserker.gujeuk.domain.purpose.domain;

import com.example.burserker.gujeuk.domain.organ.domain.Organ;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationStatus;
import com.example.burserker.gujeuk.global.entity.BaseIdEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Purpose extends BaseIdEntity {

    @Column(name = "purpose", nullable = false, length = 30)
    private String purposeName;

    @Column(nullable = false)
    private int purposeIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organ_id", nullable = false)
    private Organ organ;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus reservationStatus;

    public void updatePurpose(String purposeName, ReservationStatus reservationStatus) {
        this.purposeName = purposeName;
        this.reservationStatus = reservationStatus;
    }

    public void updatePurposeIndex(int purposeIndex) {
        this.purposeIndex = purposeIndex;
    }

    public boolean isReservable() {
        return this.reservationStatus == ReservationStatus.RESERVATION;
    }

    public void changeToSubInclude() {
        this.reservationStatus = ReservationStatus.SUB_INCLUDE;
    }
}
