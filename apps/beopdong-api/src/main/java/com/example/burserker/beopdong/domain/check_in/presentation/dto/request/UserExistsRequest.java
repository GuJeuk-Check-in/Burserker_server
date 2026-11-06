package com.example.burserker.beopdong.domain.check_in.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserExistsRequest(
        @NotBlank(message = "전화번호를 입력해주세요.")
        String phone
) {
}