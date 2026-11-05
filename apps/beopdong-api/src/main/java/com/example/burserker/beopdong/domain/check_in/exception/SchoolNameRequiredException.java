package com.example.burserker.beopdong.domain.check_in.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class SchoolNameRequiredException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new SchoolNameRequiredException();

    private SchoolNameRequiredException() {
        super(ErrorCode.BAD_REQUEST, "검색할 학교명을 입력해주세요.");
    }
}