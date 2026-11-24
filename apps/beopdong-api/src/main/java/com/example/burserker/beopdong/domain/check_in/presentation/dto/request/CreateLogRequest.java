package com.example.burserker.beopdong.domain.check_in.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateLogRequest(
        @NotNull(message = "빈칸을 채워주세요!")
        Long userId,

        @NotEmpty(message = "빈칸을 채워주세요!")
        List<@NotNull(message = "빈칸을 채워주세요!") Long> purpose
) {
}
