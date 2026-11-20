package com.example.burserker.beopdong.domain.log.presentation.dto.response;

import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.user.domain.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record OrganLogDetailResponse(
        List<Long> purpose,
        String name,
        Gender gender,
        int age,
        String phone,
        LocalDate visitDate,
        @JsonFormat(pattern = "HH:mm") LocalTime visitTime,
        String visitPurpose,
        String note
) {
    public static OrganLogDetailResponse from(Log log, List<Long> purpose) {
        return new OrganLogDetailResponse(purpose, log.getName(), log.getGender(), log.getAge(),
                log.getPhone() == null ? null : log.getPhone().replaceFirst("^(010)(\\d{4})(\\d{4})$", "$1-$2-$3"),
                log.getVisitAt().toLocalDate(), log.getVisitAt().toLocalTime(), log.getVisitPurpose(), log.getNote());
    }
}
