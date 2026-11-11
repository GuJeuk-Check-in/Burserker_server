package com.example.burserker.beopdong.domain.log.domain;

import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
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
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_visit_purpose", columnNames = {"log_id", "purpose_id"}))
public class VisitPurpose extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private Log log;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purpose_id", nullable = false)
    private Purpose purpose;

    /**
     * 한 방문에서 선택한 이용목적을 저장한다. 실제 신청·이용 기록과는 분리한다.
     */
    public static VisitPurpose create(Log log, Purpose purpose) {
        return VisitPurpose.builder().log(log).purpose(purpose).build();
    }
}
