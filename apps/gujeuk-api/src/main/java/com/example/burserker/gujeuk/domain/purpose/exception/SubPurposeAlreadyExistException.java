package com.example.burserker.gujeuk.domain.purpose.exception;

import com.example.burserker.gujeuk.global.error.exception.ErrorCode;
import com.example.burserker.gujeuk.global.error.exception.GujeukException;

public class SubPurposeAlreadyExistException extends GujeukException {
    public static final GujeukException EXCEPTION = new SubPurposeAlreadyExistException();

    private SubPurposeAlreadyExistException() {
        super(ErrorCode.SUB_PURPOSE_ALREADY_EXIST);
    }
}
