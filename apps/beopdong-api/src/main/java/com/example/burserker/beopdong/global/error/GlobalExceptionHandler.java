package com.example.burserker.beopdong.global.error;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.burserker.beopdong.domain.user.presentation.dto.request.UserSignUpRequest;
import org.springframework.validation.FieldError;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BeopdongException.class)
    public ResponseEntity<ErrorResponse> handClimException(BeopdongException e) {

        ErrorCode errorCode = e.getErrorCode();
        String description = e.getMessage() != null ? e.getMessage() : errorCode.getErrorMessage();
        ErrorResponse response = ErrorResponse.of(errorCode, description);
        e.printStackTrace();

        return new ResponseEntity<>(response, HttpStatus.valueOf(errorCode.getStatusCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        ErrorCode errorCode = ErrorCode.BAD_REQUEST;
        String description = "필수 입력 항목을 기재해 주세요.";

        if(e.getParameter().getParameterType() == UserSignUpRequest.class) {
            errorCode = ErrorCode.SIGN_UP_REQUIRED;
            description = "빈칸을 채워주세요.";

            List<FieldError> errors = e.getBindingResult().getFieldErrors();

            boolean requiredMissing = errors.stream()
                    .anyMatch(error ->
                            "NotBlank".equals(error.getCode()) ||
                                    "NotNull".equals(error.getCode()) ||
                                    "NotEmpty".equals(error.getCode())
                    );

            if(!requiredMissing) {
                for(FieldError error : errors) {
                    if("birthYMD".equals(error.getField()) &&
                            "PastOrPresent".equals(error.getCode())) {
                        errorCode = ErrorCode.SIGN_UP_BIRTH_INVALID;
                        description = "생년월일을 다시 확인해 주세요.";
                        break;
                    }

                    if("phone".equals(error.getField()) &&
                            "Pattern".equals(error.getCode())) {
                        errorCode = ErrorCode.SIGN_UP_PHONE_INVALID;
                        description = "전화번호를 다시 확인해 주세요.";
                        break;
                    }

                    if("grade".equals(error.getField()) &&
                            ("Min".equals(error.getCode()) || "Max".equals(error.getCode()))) {
                        errorCode = ErrorCode.SIGN_UP_GRADE_INVALID;
                        description = "학년을 다시 확인해 주세요.";
                        break;
                    }
                }
            }
        }

        ErrorResponse response = ErrorResponse.of(errorCode, description);

        return new ResponseEntity<>(
                response,
                HttpStatus.valueOf(errorCode.getStatusCode())
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;
        ErrorResponse response = ErrorResponse.of(errorCode, "요청 본문 형식이 올바르지 않습니다.");

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


}