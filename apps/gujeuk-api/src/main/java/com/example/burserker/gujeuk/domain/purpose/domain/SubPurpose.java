package com.example.burserker.gujeuk.domain.purpose.domain;

import com.example.burserker.gujeuk.global.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sub_purpose", uniqueConstraints = @UniqueConstraint(
        name = "uk_sub_purpose_name", columnNames = {"purpose_id", "sub_purpose"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubPurpose extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purpose_id", nullable = false)
    private Purpose purpose;

    @Column(name = "sub_purpose", nullable = false, length = 30)
    private String subPurposeName;

    @Column(name = "sub_purpose_index", nullable = false)
    private int subPurposeIndex;

    @Column(name = "reservation_status", nullable = false)
    private boolean reservationStatus;

    public boolean isReservable() {
        return this.reservationStatus;
    }

    public void updateSubPurposeIndex(int subPurposeIndex) {
        this.subPurposeIndex = subPurposeIndex;
    }
}
