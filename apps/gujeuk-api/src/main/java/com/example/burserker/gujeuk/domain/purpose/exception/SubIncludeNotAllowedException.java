package com.example.burserker.gujeuk.domain.purpose.exception;

import com.example.burserker.gujeuk.global.error.exception.ErrorCode;
import com.example.burserker.gujeuk.global.error.exception.GujeukException;

public class SubIncludeNotAllowedException extends GujeukException {
    public static final GujeukException EXCEPTION = new SubIncludeNotAllowedException();

    private SubIncludeNotAllowedException() {
        super(ErrorCode.SUB_INCLUDE_NOT_ALLOWED);
    }
}
