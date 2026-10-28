package com.example.burserker.beopdong.domain.organ.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "기존 비밀번호를 입력해주세요.")
        String oldPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
        String confirmNewPassword
) {

}