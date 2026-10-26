package com.example.burserker.beopdong.domain.user.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class InvalidTokenException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new InvalidTokenException();

    public InvalidTokenException() { super(ErrorCode.INVALID_TOKEN); }
}
