package com.example.burserker.gujeuk.domain.reservation.exception;

import com.example.burserker.gujeuk.global.error.exception.ErrorCode;
import com.example.burserker.gujeuk.global.error.exception.GujeukException;

public class ReservationAlreadyExistException extends GujeukException {
    public static final GujeukException EXCEPTION = new ReservationAlreadyExistException();

    public ReservationAlreadyExistException() {
        super(ErrorCode.RESERVATION_ALREADY_EXIST);
    }
}
