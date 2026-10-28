package com.example.burserker.beopdong.global.error.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {

    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "만료된 토큰입니다."),
    TOKEN_NOT_FOUND(401, "인증정보가 비어있습니다."),

    ORGAN_NOT_FOUND(404, "해당 기관 계정이 존재하지 않습니다."),
    SAME_OLD_PASSWORD(400, "기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다."),
    INVALID_PASSWORD_CONFIRM(400, "비밀번호 확인이 일치하지 않습니다."),

    BAD_REQUEST(400, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다."),

    PASSWORD_MISMATCH(401, "비밀 번호가 일치 하지 않습니다."),

    PURPOSE_NOT_FOUND(404, "해당 이용대장이 존재하지 않습니다.");

    private final int statusCode;
    private final String ErrorMessage;
}