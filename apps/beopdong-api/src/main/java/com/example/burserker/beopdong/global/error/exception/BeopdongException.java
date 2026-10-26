package com.example.burserker.beopdong.global.error.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BeopdongException extends RuntimeException {
    private final ErrorCode errorCode;

    public BeopdongException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}