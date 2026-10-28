package com.example.burserker.beopdong.domain.organ.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class InvalidPasswordConfirmException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new InvalidPasswordConfirmException();

    private InvalidPasswordConfirmException() {
        super(ErrorCode.INVALID_PASSWORD_CONFIRM);
    }
}
