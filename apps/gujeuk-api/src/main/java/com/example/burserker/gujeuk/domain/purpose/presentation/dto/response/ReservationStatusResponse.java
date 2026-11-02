package com.example.burserker.gujeuk.domain.purpose.presentation.dto.response;

import java.util.List;

public record ReservationStatusResponse (
    long inUseCount,
    long availableCount,
    List<ReservationResponse> reservations
){

}