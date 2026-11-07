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

    PURPOSE_NOT_FOUND(404, "해당 이용대장이 존재하지 않습니다."),
    PURPOSE_NOT_MODIFIABLE(400, "해당 이용대장은 수정하거나 삭제할 수 없습니다."),

    USER_NOT_FOUND(404, "해당 회원이 존재하지 않습니다."),
    ROUTE_DETAIL_REQUIRED(400, "기타 방문 경로를 입력해주세요."),
    USER_NAME_NOT_FOUND(404, "해당 이름이 존재하지 않습니다."),
    USER_PHONE_ALREADY_EXISTS(409, "이미 등록된 전화번호입니다."),

    SIGN_UP_BIRTH_INVALID(400, "생년월일을 다시 확인해 봐요!"),
    SIGN_UP_PHONE_INVALID(400, "전화번호를 다시 확인해 봐요!"),
    SIGN_UP_GRADE_INVALID(400, "학년을 다시 확인해 봐요!"),
    SIGN_UP_REQUIRED(400, "빈칸을 채워줘요!"),

    LOG_NOT_FOUND(404, "방명록을 찾을 수 없습니다."),
    USAGE_ALREADY_ACTIVE(409, "이미 이용 중인 시설이 있거나 선택한 좌석 또는 시설이 사용 중입니다."),
    DAILY_USAGE_EXCEEDED(409, "해당 시설은 하루에 한 번만 이용할 수 있습니다."),
    INVALID_FORM_PURPOSE(400, "이용대장을 다시 선택 해주세요!");

    private final int statusCode;
    private final String ErrorMessage;
}