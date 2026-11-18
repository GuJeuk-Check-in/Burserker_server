package com.example.burserker.beopdong.domain.organ.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class InvalidCompletedUsageException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new InvalidCompletedUsageException();

    private InvalidCompletedUsageException() {
        super(ErrorCode.BAD_REQUEST, "필수 입력 항목을 기재해주세요.");
    }
}
