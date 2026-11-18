package com.example.burserker.beopdong.domain.check_in.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class InvalidFormPurposeException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new InvalidFormPurposeException();

    private InvalidFormPurposeException() {
        super(ErrorCode.INVALID_FORM_PURPOSE);
    }
}
