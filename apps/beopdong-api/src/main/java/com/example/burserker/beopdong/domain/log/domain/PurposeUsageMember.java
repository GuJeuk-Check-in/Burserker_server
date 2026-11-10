package com.example.burserker.beopdong.domain.log.domain;

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
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_purpose_usage_member", columnNames = {"purpose_usage_id", "log_id"}))
public class PurposeUsageMember extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purpose_usage_id", nullable = false)
    private PurposeUsage purposeUsage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private Log log;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    public static PurposeUsageMember create(PurposeUsage usage, Log log, LocalDateTime now) {
        return PurposeUsageMember.builder().purposeUsage(usage).log(log).joinedAt(now).build();
    }
}
