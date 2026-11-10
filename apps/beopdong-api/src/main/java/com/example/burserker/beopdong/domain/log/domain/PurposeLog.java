package com.example.burserker.beopdong.domain.log.domain;

import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
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

    /**
     * 종료된 회차의 이름·인원·시간을 팀당 한 건의 완료 신청서로 복사한다. 원본 참가자 관계는 유지한다.
     */
    public static PurposeLog complete(PurposeUsage usage) {
        return PurposeLog.builder()
                .log(usage.getMembers().get(0).getLog())
                .purpose(usage.getPurpose())
                .purposeUsage(usage)
                .purposeState(PurposeState.SUCCESS)
                .name(usage.getName())
                .seat(usage.getSeat())
                .usePurpose(usage.getUsePurpose())
                .count(usage.getSeat() == null ? usage.getCount() : null)
                .startAt(usage.getStartAt())
                .endAt(usage.getEndAt())
                .build();
    }

    /**
     * 관리자가 완료 신청서의 표시값을 수정한다. 회원·방명록과 실제 참가자 정보는 변경하지 않는다.
     */
    public void updateCompletedUsage(Purpose purpose, String name, String usePurpose, Integer seat,
                                     Integer count, LocalDateTime startAt, LocalDateTime endAt) {
        this.purpose = purpose;
        this.name = name;
        this.usePurpose = usePurpose;
        this.seat = seat;
        this.count = count;
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
