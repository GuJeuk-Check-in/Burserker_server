package com.example.burserker.gujeuk.domain.purpose.presentation.dto.response;

import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationTime;
import java.util.List;

public record AvailableReservationTimeListResponse(
        List<ReservationTime> times
) {
}
