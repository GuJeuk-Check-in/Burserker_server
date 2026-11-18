package com.example.burserker.beopdong.domain.check_in.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class InvalidVisitPurposeException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new InvalidVisitPurposeException();

    private InvalidVisitPurposeException() {
        super(ErrorCode.INVALID_FORM_PURPOSE, "이용대장을 다시 선택 해주세요");
    }
}
