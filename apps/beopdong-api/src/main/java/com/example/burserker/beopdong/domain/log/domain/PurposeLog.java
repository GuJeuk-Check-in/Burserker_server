package com.example.burserker.beopdong.domain.log.domain;

import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.PurposeUsage;
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
public class PurposeLog extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private Log log;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purpose_id", nullable = false)
    private Purpose purpose;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purpose_usage_id", unique = true)
    private PurposeUsage purposeUsage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurposeState purposeState;

    @Column(columnDefinition = "TEXT")
    private String name;

    private Integer seat;

    private String usePurpose;

    private Integer count;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    public static PurposeLog create(PurposeUsage usage) {
        PurposeLog history = PurposeLog.builder()
                .purposeUsage(usage)
                .purposeState(PurposeState.PENDING)
                .build();
        history.updateUsage(usage);
        return history;
    }

    public void updateUsage(PurposeUsage usage) {
        this.log = usage.getLogs().get(0);
        this.purpose = usage.getPurpose();
        this.name = usage.getName();
        this.seat = usage.getSeat();
        this.usePurpose = usage.getUsePurpose();
        this.count = usage.getSeat() == null ? usage.getCount() : null;
        this.startAt = usage.getStartAt();
        this.endAt = usage.getEndAt();
    }

    public boolean isCompleted(LocalDateTime now) {
        return purposeState == PurposeState.SUCCESS
                || (purposeState == PurposeState.PENDING && purposeUsage != null && !now.isBefore(purposeUsage.getEndAt()));
    }

    public void endEarly(LocalDateTime now) {
        this.purposeState = PurposeState.SUCCESS;
        this.endAt = now;
    }

    /**
     * 관리자가 완료 신청서의 표시값을 수정한다. 회원·방명록과 실제 참가자 정보는 변경하지 않는다.
     */
    public void updateCompletedUsage(Purpose purpose, String name, String usePurpose, Integer seat,
                                     Integer count, LocalDateTime startAt, LocalDateTime endAt) {
        this.purposeState = PurposeState.SUCCESS;
        this.purpose = purpose;
        this.name = name;
        this.usePurpose = usePurpose;
        this.seat = seat;
        this.count = count;
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
