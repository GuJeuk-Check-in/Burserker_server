package com.example.burserker.gujeuk.domain.purpose.exception;

import com.example.burserker.gujeuk.global.error.exception.ErrorCode;
import com.example.burserker.gujeuk.global.error.exception.GujeukException;

public class SubPurposeNotFoundException extends GujeukException {
    public static final GujeukException EXCEPTION = new SubPurposeNotFoundException();

    private SubPurposeNotFoundException() {
        super(ErrorCode.SUB_PURPOSE_NOT_FOUND);
    }
}
