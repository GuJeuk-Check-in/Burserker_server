package com.example.burserker.beopdong.domain.checkin.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserExistsRequest(
        @NotBlank
        String phone
) {
}