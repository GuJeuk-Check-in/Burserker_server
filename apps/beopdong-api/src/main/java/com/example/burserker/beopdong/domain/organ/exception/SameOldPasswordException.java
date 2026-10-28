package com.example.burserker.beopdong.domain.organ.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class SameOldPasswordException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new SameOldPasswordException();

    private SameOldPasswordException() {
        super(ErrorCode.SAME_OLD_PASSWORD);
    }
}
