package com.example.burserker.gujeuk.domain.purpose.domain;

import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationTime;
import com.example.burserker.gujeuk.domain.user.domain.User;
import com.example.burserker.gujeuk.global.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purpose_reservation", uniqueConstraints = @UniqueConstraint(
        name = "uk_reservation_slot",
        columnNames = {"purpose_id", "slot_sub_purpose_id", "reservation_date", "start_time"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurposeReservation extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purpose_id", nullable = false)
    private Purpose purpose;

    // 하위 방문목적 단위 예약이면 채운다. 상위 방문목적 단위 예약이면 null이다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_purpose_id")
    private SubPurpose subPurpose;

    // DB 생성 컬럼: IFNULL(sub_purpose_id, 0). 슬롯 중복 판별에만 쓰며 직접 쓰지 않는다.
    @Column(name = "slot_sub_purpose_id", insertable = false, updatable = false)
    private Long slotSubPurposeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_time", nullable = false, length = 20)
    private ReservationTime startTime;

    @Column(name = "representative_phone")
    private String representativePhone;

    @Column(name = "male_count", nullable = false)
    private int maleCount;

    @Column(name = "female_count", nullable = false)
    private int femaleCount;
}
