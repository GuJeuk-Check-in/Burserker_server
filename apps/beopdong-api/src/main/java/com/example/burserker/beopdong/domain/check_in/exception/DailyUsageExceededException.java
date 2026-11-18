package com.example.burserker.beopdong.domain.check_in.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class DailyUsageExceededException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new DailyUsageExceededException();

    private DailyUsageExceededException() {
        super(ErrorCode.DAILY_USAGE_EXCEEDED);
    }
}
