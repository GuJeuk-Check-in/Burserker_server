package com.example.burserker.gujeuk.domain.reservation.exception;

import com.example.burserker.gujeuk.global.error.exception.ErrorCode;
import com.example.burserker.gujeuk.global.error.exception.GujeukException;

public class ReservationNotFoundException extends GujeukException {
    public static final GujeukException EXCEPTION = new ReservationNotFoundException();

    private ReservationNotFoundException() { super(ErrorCode.RESERVATION_NOT_FOUND); }
}
