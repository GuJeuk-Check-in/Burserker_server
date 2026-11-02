package com.example.burserker.beopdong.domain.purpose.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PurposeMoveRequest(
        @NotEmpty(message = "방문 목적 순서가 유효하지 않습니다.")
        List<@NotNull Long> purposeId
) {
}
