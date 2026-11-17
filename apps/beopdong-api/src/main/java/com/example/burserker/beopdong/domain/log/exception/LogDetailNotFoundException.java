package com.example.burserker.beopdong.domain.log.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class LogDetailNotFoundException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new LogDetailNotFoundException();

    private LogDetailNotFoundException() {
        super(ErrorCode.LOG_DETAIL_NOT_FOUND, "존재하지 않는 방명록 입니다.");
    }
}
