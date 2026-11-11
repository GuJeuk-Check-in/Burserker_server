package com.example.burserker.beopdong.domain.organ.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class InvalidUsagePageException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new InvalidUsagePageException();

    private InvalidUsagePageException() {
        super(ErrorCode.BAD_REQUEST, "page는 0 이상, size는 30이어야 합니다.");
    }
}
