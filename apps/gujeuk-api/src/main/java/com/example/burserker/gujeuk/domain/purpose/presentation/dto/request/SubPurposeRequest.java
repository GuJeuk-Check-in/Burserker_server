package com.example.burserker.gujeuk.domain.purpose.presentation.dto.request;

import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubPurposeRequest(
        @Size(min = 1, max = 30, message = "방문 목적은 1~30자로 입력해주세요.")
        @NotBlank(message = "방문 목적을 입력해주세요.")
        String purpose,

        @NotNull(message = "예약 필요 여부를 선택해주세요.")
        ReservationStatus reservationStatus,

        @Size(min = 1, max = 30, message = "하위 방문목적은 1~30자로 입력해주세요.")
        @NotBlank(message = "하위 방문목적을 입력해주세요.")
        String subPurpose,

        @NotNull(message = "하위 방문목적의 예약 필요 여부를 선택해주세요.")
        Boolean subPurposeReservation
) {
}
