package com.example.burserker.beopdong.domain.user.presentation.dto.request;

import com.example.burserker.beopdong.domain.user.domain.enums.Gender;
import com.example.burserker.beopdong.domain.user.domain.enums.Route;
import com.example.burserker.beopdong.domain.user.domain.enums.SchoolType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UpdateUserRequest(
        @NotBlank
        String name,

        @NotNull
        @Min(0)
        @Max(6)
        Integer grade,

        @NotNull
        Route route,

        String routeDetail,

        @NotBlank
        String address,

        @NotBlank
        String school,

        @NotNull
        Gender gender,

        @NotBlank
        @Pattern(regexp = "^010[0-9]{8}$")
        String phone,

        @NotNull
        @PastOrPresent
        LocalDate birthYMD,

        @NotNull
        SchoolType schoolType,

        @NotNull
        @PastOrPresent
        LocalDate applyDate
) {
}