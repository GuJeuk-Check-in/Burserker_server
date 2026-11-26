package com.example.burserker.gujeuk.domain.purpose.exception;

import com.example.burserker.gujeuk.global.error.exception.ErrorCode;
import com.example.burserker.gujeuk.global.error.exception.GujeukException;

public class PurposeStatusFixedException extends GujeukException {
    public static final GujeukException EXCEPTION = new PurposeStatusFixedException();

    private PurposeStatusFixedException() {
        super(ErrorCode.PURPOSE_STATUS_FIXED);
    }
}
