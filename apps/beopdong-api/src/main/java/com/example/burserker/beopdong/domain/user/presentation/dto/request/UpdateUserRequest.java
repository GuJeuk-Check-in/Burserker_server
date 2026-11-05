package com.example.burserker.beopdong.domain.user.presentation.dto.request;

import com.example.burserker.beopdong.domain.user.domain.enums.Gender;
import com.example.burserker.beopdong.domain.user.domain.enums.Route;
import com.example.burserker.beopdong.domain.user.domain.enums.SchoolType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UpdateUserRequest(
        @NotBlank(message = "이름을 입력해주세요.")
        String name,

        @NotNull(message = "학년을 선택해주세요.")
        @Min(value = 1, message = "학년을 다시 확인해 주세요.")
        @Max(value = 6, message = "학년을 다시 확인해 주세요.")
        Integer grade,

        @NotNull(message = "문화센터를 알게 된 경로를 입력해주세요.")
        Route route,

        String routeDetail,

        @NotBlank(message = "주소를 입력해주세요.")
        String address,

        @NotBlank(message = "학교를 입력해주세요.")
        String school,

        @NotNull(message = "성별을 입력해주세요.")
        Gender gender,

        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = "^010[0-9]{8}$", message = "전화번호를 다시 입력해주세요.")
        String phone,

        @NotNull(message = "생년월일을 입력해주세요.")
        @PastOrPresent(message = "생년월일을 다시 입력해주세요.")
        LocalDate birthYMD,

        @NotNull(message = "학교급을 선택해주세요.")
        SchoolType schoolType,

        @NotNull(message = "가입일을 입력해주세요.")
        @PastOrPresent(message = "가입일을 다시 확인해주세요.")
        LocalDate applyDate
) {
}