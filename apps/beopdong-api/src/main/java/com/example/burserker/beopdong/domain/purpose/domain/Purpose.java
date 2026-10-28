package com.example.burserker.beopdong.domain.purpose.domain;

import com.example.burserker.beopdong.domain.organ.domain.Organ;
import com.example.burserker.beopdong.global.entity.BaseIdEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public void updatePurpose(String purposeName) {
        this.purposeName = purposeName;
    }

    public void updatePurposeIndex(int purposeIndex) {
        this.purposeIndex = purposeIndex;
    }
}