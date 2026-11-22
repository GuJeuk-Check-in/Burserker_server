package com.example.burserker.beopdong.domain.log.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class InvalidLogPageException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new InvalidLogPageException();

    private InvalidLogPageException() {
        super(ErrorCode.BAD_REQUEST, "page 또는 size 값이 올바르지 않음");
    }
}
