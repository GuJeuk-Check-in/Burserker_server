package com.example.burserker.beopdong.domain.log.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class CheckOutUsageActiveException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new CheckOutUsageActiveException();

    private CheckOutUsageActiveException() {
        super(ErrorCode.CHECK_OUT_USAGE_ACTIVE);
    }
}
