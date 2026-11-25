package com.example.burserker.beopdong.domain.check_in.presentation.dto.request;

import com.example.burserker.beopdong.domain.user.domain.enums.Gender;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateAdultLogRequest(
        @NotNull(message = "빈칸을 채워주세요!")
        Gender gender,

        @NotEmpty(message = "빈칸을 채워주세요!")
        List<@NotNull(message = "빈칸을 채워주세요!") Long> purpose
) {
}
