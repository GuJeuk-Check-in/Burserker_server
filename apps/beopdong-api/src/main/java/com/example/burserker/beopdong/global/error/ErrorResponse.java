package com.example.burserker.beopdong.global.error;

import com.example.burserker.beopdong.global.error.exception.ErrorCode;

import java.time.LocalDateTime;

public record ErrorResponse (
        String message,
        Integer status,
        LocalDateTime timestamp,
        String description

) {
    public static ErrorResponse of(ErrorCode errorCode, String description) {
        return new ErrorResponse(
                errorCode.getErrorMessage(),
                errorCode.getStatusCode(),
                LocalDateTime.now(),
                description
        );
    }
}
