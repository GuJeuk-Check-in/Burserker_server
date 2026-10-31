package com.example.burserker.beopdong.domain.purpose.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PurposeRequest(
        @NotBlank(message = "빈칸을 채워주세요.")
        @Size(max = 30, message = "30자 이하로 입력해주세요.")
        String purposeName
) {
}