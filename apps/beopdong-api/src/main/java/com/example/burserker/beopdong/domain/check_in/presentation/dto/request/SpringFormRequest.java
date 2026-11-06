package com.example.burserker.beopdong.domain.check_in.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SpringFormRequest(
        @NotNull(message = "방명록 정보를 확인해주세요.")
        Long logId,

        @NotNull(message = "이용대장을 선택해주세요.")
        Long purpose,

        @NotNull(message = "이용 인원을 입력해주세요.")
        @Min(value = 1, message = "이용 인원은 1 ~ 2명으로 입력해주세요.")
        @Max(value = 2, message = "이용 인원은 1 ~ 2명으로 입력해주세요.")
        Integer count
) {
}
