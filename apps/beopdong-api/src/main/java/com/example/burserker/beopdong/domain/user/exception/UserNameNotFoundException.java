package com.example.burserker.beopdong.domain.user.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class UserNameNotFoundException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new UserNameNotFoundException();

    private UserNameNotFoundException() {
        super(ErrorCode.USER_NAME_NOT_FOUND);
    }
}