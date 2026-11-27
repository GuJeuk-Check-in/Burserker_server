package com.example.burserker.gujeuk.domain.reservation.presentation.dto.response;

import com.example.burserker.gujeuk.domain.purpose.domain.PurposeReservation;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record ReservationResponse (
    Long id,
    Long purposeId,
    String purpose,
    Long subPurposeId,
    String subPurpose,
    String name,
    int maleCount,
    int femaleCount,
    String phone,

    @JsonFormat(pattern = "HH:mm:ss") LocalTime startTime,
    @JsonFormat(pattern = "HH:mm:ss") LocalTime endTime

){
    public static ReservationResponse from(PurposeReservation reservation){
        return of(reservation, true);
    }

    // 로그인 없는 현황판 응답. 예약자 전화번호를 내보내지 않는다.
    public static ReservationResponse withoutPhone(PurposeReservation reservation){
        return of(reservation, false);
    }

    private static ReservationResponse of(PurposeReservation reservation, boolean includePhone){
        LocalTime startTime = reservation.getStartTime().getTime();

        return new ReservationResponse(
                reservation.getId(),
                reservation.getPurpose().getId(),
                reservation.getPurpose().getPurposeName(),
                reservation.getSubPurpose() == null ? null : reservation.getSubPurpose().getId(),
                reservation.getSubPurpose() == null ? null : reservation.getSubPurpose().getSubPurposeName(),
                reservation.getName(),
                reservation.getMaleCount(),
                reservation.getFemaleCount(),
                includePhone ? reservation.getRepresentativePhone() : null,
                startTime,
                startTime.plusHours(1)
        );
    }
}
