package com.example.burserker.gujeuk.domain.purpose.presentation.dto.response;

import java.util.List;

public record TodayReservationListResponse(
        List<TodayReservationResponse> purposes
) {
}
