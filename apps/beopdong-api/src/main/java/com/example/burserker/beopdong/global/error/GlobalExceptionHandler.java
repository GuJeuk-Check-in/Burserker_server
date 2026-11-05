package com.example.burserker.beopdong.global.error;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BeopdongException.class)
    public ResponseEntity<ErrorResponse> handClimException(BeopdongException e) {
        ErrorCode errorCode = e.getErrorCode();
        String description = e.getMessage() != null
                ? e.getMessage()
                : errorCode.getErrorMessage();

        ErrorResponse response = ErrorResponse.of(errorCode, description);
        e.printStackTrace();

        return new ResponseEntity<>(
                response,
                HttpStatus.valueOf(errorCode.getStatusCode())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(value -> value != null)
                .findFirst()
                .orElse("잘못된 요청입니다.");

        ErrorResponse response = new ErrorResponse(
                message,
                400,
                LocalDateTime.now(),
                message
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.BAD_REQUEST,
                "요청 본문 형식이 올바르지 않습니다."
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handlerException(Exception e) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        log.error("처리하지 못한 서버 오류가 발생했습니다.", e);

        ErrorResponse response = ErrorResponse.of(
                errorCode,
                errorCode.getErrorMessage()
        );

        return new ResponseEntity<>(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException e
    ) {
        Throwable cause = e;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();

                if (constraintName != null &&
                        ("uk_user_phone".equals(constraintName) ||
                                constraintName.endsWith(".uk_user_phone"))) {
                    ErrorCode errorCode = ErrorCode.USER_PHONE_ALREADY_EXISTS;

                    return ResponseEntity.status(errorCode.getStatusCode())
                            .body(ErrorResponse.of(
                                    errorCode,
                                    errorCode.getErrorMessage()
                            ));
                }
            }

            cause = cause.getCause();
        }

        return handlerException(e);
    }
}