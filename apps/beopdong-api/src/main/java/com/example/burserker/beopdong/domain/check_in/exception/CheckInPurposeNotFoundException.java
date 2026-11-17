package com.example.burserker.beopdong.domain.check_in.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class CheckInPurposeNotFoundException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new CheckInPurposeNotFoundException();

    private CheckInPurposeNotFoundException() {
        super(ErrorCode.CHECK_IN_PURPOSE_NOT_FOUND, "존재하지 않는 이용대장입니다.");
    }
}
