package com.example.burserker.beopdong.domain.check_in.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class UsageAlreadyActiveException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new UsageAlreadyActiveException();

    private UsageAlreadyActiveException() {
        super(ErrorCode.USAGE_ALREADY_ACTIVE);
    }
}
