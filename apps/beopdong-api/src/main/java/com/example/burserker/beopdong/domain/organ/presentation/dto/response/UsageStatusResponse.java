package com.example.burserker.beopdong.domain.organ.presentation.dto.response;

import java.time.LocalTime;
import java.util.List;

public record UsageStatusResponse(
        UsageStatus usageStatus
) {

    public record UsageStatus(List<PcUsage> pc, RoomUsage pingPong, RoomUsage sing) {
    }

    public record PcUsage(
            Long id,
            Integer seat,
            String name,
            LocalTime startTime,
            LocalTime endTime,
            long purposeRemainTime
    ) {
    }

    public record RoomUsage(
            Long id,
            String name,
            Integer count,
            LocalTime startTime,
            LocalTime endTime,
            long purposeRemainTime
    ) {
    }
}