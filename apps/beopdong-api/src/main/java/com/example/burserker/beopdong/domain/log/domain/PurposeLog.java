package com.example.burserker.beopdong.domain.log.domain;

import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.global.entity.BaseIdEntity;
import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
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
public class PurposeLog extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private Log log;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purpose_id", nullable = false)
    private Purpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurposeState purposeState;

    private Integer seat;

    private String usePurpose;

    private Integer count;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    public static PurposeLog create(Log log, Purpose purpose) {
        return PurposeLog.builder()
                .log(log)
                .purpose(purpose)
                .purposeState(PurposeState.NONE)
                .build();
    }
    public void startPc(
            int seat,
            String usePurpose,
            LocalDateTime startAt,
            int usageMinutes
    ) {
        this.seat = seat;
        this.usePurpose = usePurpose;
        this.count = null;
        this.startAt = startAt;
        this.endAt = startAt.plusMinutes(usageMinutes);
        this.purposeState = PurposeState.PENDING;
    }

    public void startSping(
            int count,
            LocalDateTime startAt,
            int usageMinutes
    ) {
        this.seat = null;
        this.usePurpose = null;
        this.count = count;
        this.startAt = startAt;
        this.endAt = startAt.plusMinutes(usageMinutes);
        this.purposeState = PurposeState.PENDING;
    }


    public void completeIfExpired(LocalDateTime now) {
        if (purposeState == PurposeState.PENDING
                && endAt != null
                && !now.isBefore(endAt)) {
            this.purposeState = PurposeState.SUCCESS;
        }
    }
}