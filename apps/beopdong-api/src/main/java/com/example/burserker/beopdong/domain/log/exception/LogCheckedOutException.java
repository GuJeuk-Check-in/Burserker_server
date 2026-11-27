package com.example.burserker.beopdong.domain.log.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class LogCheckedOutException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new LogCheckedOutException();

    private LogCheckedOutException() {
        super(ErrorCode.LOG_CHECKED_OUT);
    }
}
