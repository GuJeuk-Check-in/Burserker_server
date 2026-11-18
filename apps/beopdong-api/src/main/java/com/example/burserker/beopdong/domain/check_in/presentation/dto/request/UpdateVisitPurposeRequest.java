package com.example.burserker.beopdong.domain.check_in.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateVisitPurposeRequest(
        @NotNull(message = "빈칸을 채워주세요!")
        Long logId,

        @NotEmpty(message = "빈칸을 채워주세요!")
        List<@NotNull(message = "빈칸을 채워주세요!") Long> purpose
) {
}
