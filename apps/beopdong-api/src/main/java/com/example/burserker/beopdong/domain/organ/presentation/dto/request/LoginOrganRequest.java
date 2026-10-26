package com.example.burserker.beopdong.domain.organ.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginOrganRequest (
    @NotBlank(message = "기관명을 입력해주세요.")
    String organName,

    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password
) {
}