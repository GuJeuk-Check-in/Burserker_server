package com.example.burserker.beopdong.domain.organ.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateCompletedUsageRequest(
        @NotNull(message = "필수 입력 항목을 기재해주세요.")
        Long purpose,

        @NotBlank(message = "필수 입력 항목을 기재해주세요.")
        String name,

        @NotNull(message = "필수 입력 항목을 기재해주세요.")
        LocalDate visitDate,

        String usePurpose,

        @NotNull(message = "필수 입력 항목을 기재해주세요.")
        LocalTime startTime,

        @NotNull(message = "필수 입력 항목을 기재해주세요.")
        LocalTime endTime,

        Integer seat,
        Integer count
) {
}
