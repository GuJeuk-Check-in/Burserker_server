package com.example.burserker.beopdong.domain.check_in.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SpringFormRequest(
        @NotNull(message = "빈칸을 채워주세요.")
        Long logId,

        @NotNull(message = "빈칸을 채워주세요.")
        Long purpose,

        @NotNull(message = "이용 인원을 입력해주세요.")
        @Min(value = 1, message = "이용 인원을 다시 확인해주세요.")
        @Max(value = 4, message = "이용 인원을 다시 확인해주세요.")
        Integer count
) {
}