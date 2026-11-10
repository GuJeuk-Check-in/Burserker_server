package com.example.burserker.beopdong.domain.check_in.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record SpringFormRequest(
        @NotNull(message = "빈칸을 채워주세요.")
        Long logId,

        @NotNull(message = "빈칸을 채워주세요.")
        Long purpose
) {
}
