package com.example.burserker.gujeuk.domain.purpose.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PurposeRequest(
        @Size(min = 1, max = 30)
        @NotBlank(message = "방문 목적을 입력해주세요.")
        String purpose,

        @NotNull(message = "예약 필요 여부를 선택해주세요.")
        Boolean reservationStatus
) {
}
