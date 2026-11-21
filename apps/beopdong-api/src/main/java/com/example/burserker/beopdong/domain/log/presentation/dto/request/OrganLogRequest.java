package com.example.burserker.beopdong.domain.log.presentation.dto.request;

import com.example.burserker.beopdong.domain.user.domain.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record OrganLogRequest(
        String name,
        Integer age,
        Gender gender,
        @Pattern(regexp = "^(?:\\s*|010\\d{8})$", message = "연락처 형식이 올바르지 않음")
        String phone,
        @NotNull(message = "방문날짜는 필수 입력값입니다.")
        LocalDate visitDate,
        @NotNull(message = "이용대장은 필수 입력값입니다.")
        List<@NotNull(message = "이용대장을 다시 선택해주세요.") Long> purpose,
        @NotNull(message = "방문 시각은 필수 입력값입니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime visitTime,
        String visitPurpose,
        String note
) {
}
