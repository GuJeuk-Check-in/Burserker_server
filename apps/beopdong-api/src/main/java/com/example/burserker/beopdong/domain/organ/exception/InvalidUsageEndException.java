package com.example.burserker.beopdong.domain.organ.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class InvalidUsageEndException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new InvalidUsageEndException();

    private InvalidUsageEndException() {
        super(ErrorCode.BAD_REQUEST);
    }
}
