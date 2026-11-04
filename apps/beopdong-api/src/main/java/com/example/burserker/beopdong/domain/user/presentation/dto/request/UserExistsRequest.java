package com.example.burserker.beopdong.domain.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserExistsRequest(
        @NotBlank
        String phone
) {
}