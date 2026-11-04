package com.example.burserker.beopdong.domain.user.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class RouteDetailRequiredException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new RouteDetailRequiredException();

    private RouteDetailRequiredException() {
        super(ErrorCode.ROUTE_DETAIL_REQUIRED);
    }
}