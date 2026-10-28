package com.example.burserker.beopdong.domain.purpose.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class PurposeNotFoundException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new PurposeNotFoundException();

    private PurposeNotFoundException() {
        super(ErrorCode.PURPOSE_NOT_FOUND);
    }
}
