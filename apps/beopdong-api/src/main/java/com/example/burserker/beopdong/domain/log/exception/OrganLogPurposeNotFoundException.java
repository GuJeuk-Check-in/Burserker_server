package com.example.burserker.beopdong.domain.log.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class OrganLogPurposeNotFoundException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new OrganLogPurposeNotFoundException();

    private OrganLogPurposeNotFoundException() {
        super(ErrorCode.ORGAN_LOG_PURPOSE_NOT_FOUND);
    }
}
