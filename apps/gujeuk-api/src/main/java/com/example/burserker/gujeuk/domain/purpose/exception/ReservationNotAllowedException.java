package com.example.burserker.gujeuk.domain.purpose.exception;

import com.example.burserker.gujeuk.global.error.exception.ErrorCode;
import com.example.burserker.gujeuk.global.error.exception.GujeukException;

public class ReservationNotAllowedException extends GujeukException {
    public static final GujeukException EXCEPTION = new ReservationNotAllowedException();

    private ReservationNotAllowedException() {
        super(ErrorCode.RESERVATION_NOT_ALLOWED);
    }
}
