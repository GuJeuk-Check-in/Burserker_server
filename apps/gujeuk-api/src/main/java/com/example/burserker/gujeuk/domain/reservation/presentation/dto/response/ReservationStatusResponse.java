package com.example.burserker.gujeuk.domain.reservation.presentation.dto.response;

import java.util.List;

public record ReservationStatusResponse (
    int inUseCount,
    int availableCount,
    List<ReservationResponse> reservations
){

}