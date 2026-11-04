package com.example.burserker.beopdong.domain.user.presentation.dto.response;

import com.example.burserker.beopdong.domain.user.domain.enums.SchoolType;

public record SchoolResponse(
        String educationOfficeCode,
        String schoolCode,
        String schoolName,
        SchoolType schoolType,
        String address
) {
}