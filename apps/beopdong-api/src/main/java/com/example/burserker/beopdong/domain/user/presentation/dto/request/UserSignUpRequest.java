package com.example.burserker.beopdong.domain.user.presentation.dto.request;

import com.example.burserker.beopdong.domain.user.domain.enums.Gender;
import com.example.burserker.beopdong.domain.user.domain.enums.Route;
import com.example.burserker.beopdong.domain.user.domain.enums.SchoolType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record UserSignUpRequest(
        @NotBlank
        String name,

        @NotNull
        Gender gender,

        @NotBlank
        @Pattern(regexp = "^010[0-9]{8}$")
        String phone,

        @NotNull
        @PastOrPresent
        LocalDate birthYMD,

        @NotNull
        @Min(0)
        @Max(6)
        Integer grade,

        @NotBlank
        String school,

        @NotNull
        Route route,

        String routeDetail,

        @NotBlank
        String address,

        @NotNull
        @AssertTrue
        Boolean privacyAgreed,

        @NotNull
        SchoolType schoolType,

        @NotEmpty
        List<@NotNull Long> purpose
) {
}