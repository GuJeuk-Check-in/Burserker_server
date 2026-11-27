package com.example.burserker.gujeuk.domain.purpose.exception;

import com.example.burserker.gujeuk.global.error.exception.ErrorCode;
import com.example.burserker.gujeuk.global.error.exception.GujeukException;

public class ReservationTargetRequiredException extends GujeukException {
    public static final GujeukException EXCEPTION = new ReservationTargetRequiredException();

    private ReservationTargetRequiredException() {
        super(ErrorCode.RESERVATION_TARGET_REQUIRED);
    }
}
