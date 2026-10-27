package com.example.burserker.gujeuk.domain.purpose.domain;

import com.example.burserker.gujeuk.domain.organ.domain.Organ;
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

    @Column(nullable = false)
    private boolean reservationStatus;

    public void updatePurpose(String purposeName, boolean reservationStatus) {
        this.purposeName = purposeName;
        this.reservationStatus = reservationStatus;
    }

    public void updatePurposeIndex(int purposeIndex) {
        this.purposeIndex = purposeIndex;
    }
}
