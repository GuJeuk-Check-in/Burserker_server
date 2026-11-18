package com.example.burserker.beopdong.domain.organ.presentation.dto.response;

import com.example.burserker.beopdong.domain.log.domain.PurposeLog;

import java.time.LocalDate;
import java.time.LocalTime;

public record CompletedUsageResponse(
        Long id,
        Long purpose,
        String name,
        Integer count,
        LocalDate visitDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer seat
) {

    /**
     * 완료 신청서 한 건을 날짜·시간·좌석·인원을 포함한 목록 항목으로 변환한다.
     */
    public static CompletedUsageResponse from(PurposeLog usage) {
        return new CompletedUsageResponse(
                usage.getId(),
                usage.getPurpose().getId(),
                usage.getName(),
                usage.getCount(),
                usage.getStartAt().toLocalDate(),
                usage.getStartAt().toLocalTime().withNano(0),
                usage.getEndAt().toLocalTime().withNano(0),
                usage.getSeat()
        );
    }
}
