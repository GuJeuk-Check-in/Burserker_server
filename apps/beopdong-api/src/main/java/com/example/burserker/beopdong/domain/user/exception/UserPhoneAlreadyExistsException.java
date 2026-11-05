package com.example.burserker.beopdong.domain.user.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class UserPhoneAlreadyExistsException extends BeopdongException {

    public static final BeopdongException EXCEPTION =
            new UserPhoneAlreadyExistsException();

    private UserPhoneAlreadyExistsException() {
        super(ErrorCode.USER_PHONE_ALREADY_EXISTS);
    }
}