package com.example.burserker.beopdong.domain.organ.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class OrganNotFoundException extends BeopdongException {
    public static final BeopdongException EXCEPTION = new OrganNotFoundException();

    private OrganNotFoundException() {super(ErrorCode.ORGAN_NOT_FOUND);}
}
