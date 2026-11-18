package com.example.burserker.beopdong.domain.check_in.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class InvalidUsageCountException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new InvalidUsageCountException();

    private InvalidUsageCountException() {
        super(ErrorCode.BAD_REQUEST, "이용 인원을 다시 확인해주세요.");
    }
}
