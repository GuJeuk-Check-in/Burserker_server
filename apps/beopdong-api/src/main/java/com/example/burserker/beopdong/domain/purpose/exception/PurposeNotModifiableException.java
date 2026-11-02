package com.example.burserker.beopdong.domain.purpose.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class PurposeNotModifiableException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new PurposeNotModifiableException();

    private PurposeNotModifiableException() {
        super(ErrorCode.PURPOSE_NOT_MODIFIABLE);
    }
}