package com.example.burserker.beopdong.domain.log.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class LogNotFoundException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new LogNotFoundException();

    private LogNotFoundException() {
        super(ErrorCode.LOG_NOT_FOUND);
    }
}
