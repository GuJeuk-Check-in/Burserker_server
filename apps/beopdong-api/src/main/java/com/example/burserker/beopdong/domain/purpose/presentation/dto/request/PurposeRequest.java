package com.example.burserker.beopdong.domain.purpose.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PurposeRequest(
        @NotBlank(message = "빈칸을 채워주세요.")
        String purposeType
) {
}