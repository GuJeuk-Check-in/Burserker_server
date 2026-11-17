package com.example.burserker.beopdong.domain.organ.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class PurposeUsageNotFoundException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new PurposeUsageNotFoundException();

    private PurposeUsageNotFoundException() {
        super(ErrorCode.PURPOSE_LOG_NOT_FOUND);
    }
}
