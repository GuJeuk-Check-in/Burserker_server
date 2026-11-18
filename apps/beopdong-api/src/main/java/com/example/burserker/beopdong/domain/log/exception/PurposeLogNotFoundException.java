package com.example.burserker.beopdong.domain.log.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class PurposeLogNotFoundException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new PurposeLogNotFoundException();

    private PurposeLogNotFoundException() {
        super(ErrorCode.PURPOSE_LOG_NOT_FOUND);
    }
}
