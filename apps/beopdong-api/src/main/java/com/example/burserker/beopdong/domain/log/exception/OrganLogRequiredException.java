package com.example.burserker.beopdong.domain.log.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class OrganLogRequiredException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new OrganLogRequiredException();

    private OrganLogRequiredException() {
        super(ErrorCode.ORGAN_LOG_REQUIRED);
    }
}
