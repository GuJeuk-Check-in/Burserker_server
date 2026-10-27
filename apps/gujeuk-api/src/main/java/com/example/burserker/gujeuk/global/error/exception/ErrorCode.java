package com.example.burserker.gujeuk.global.error.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {

    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(404, "RefreshToken이 존재 하지 않습니다."),
    TOKEN_NOT_FOUND(401, "인증정보가 비어있습니다."),

    LOG_NOT_FOUND(404, "존재하지 않는 이용목록입니다."),
    DUPLICATE_LOG(409, "이미 해당 시간의 로그가 존재합니다."),
    CLIENT_RECORD_ID_MISMATCH(409, "동일한 clientRecordId로 다른 요청 본문이 전달되었습니다."),
    INVALID_LOG_DATE(400, "유효하지 않은 날짜 형식입니다."),

    INVALID_RESIDENCE(400, "존재하지 않는 거주지 이름입니다."),

    EXCEL_GENERATION_FAILED(507, "엑셀 파일 생성 중 오류가 발생했습니다."),
    INVALID_DATE(400, "유효하지 않은 날짜 형식입니다."),

    PURPOSE_NOT_FOUND(404, "존재하지 않는 방문목적입니다."),
    PURPOSE_ALREADY_EXIST(409, "이미 존재하는 방문 목적입니다."),

    RESERVATION_NOT_ALLOWED(400, "예약할 수 없는 방문 목적입니다."),
    RESERVATION_SLOT_UNAVAILABLE(409, "이미 예약된 시간입니다."),

    USER_NOT_FOUND(404, "해당 유저가 존재 하지 않습니다."),
    PASSWORD_MISMATCH(401, "비밀 번호가 일치 하지 않습니다."),
    USER_ACCESS_DENIED(403, "해당 유저에 관한 권한이 없습니다."),

    ORGAN_NOT_FOUND(404, "해당 기관 계정이 존재하지 않습니다."),
    INVALID_PASSWORD_CONFIRM(401, "비밀번호 확인이 일치하지 않습니다."),
    SAME_OLD_PASSWORD(400, "기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다."),
    ORGAN_ALREADY_EXIST(409, "이미 기관 계정이 존재합니다."),

    RESIDENCE_ALREADY_EXIST(409, "이미 존재하는 거주지 입니다."),
    RESIDENCE_NOT_FOUND(404, "존재하지 않는 거주지 입니다."),
    RESIDENCE_ACCESS_DENIED(403, "해당 거주지에 관한 권한이 없습니다."),

    INVALID_CHECK_IN_FUNNEL_EVENT_NAME(400, "유효하지 않은 체크인 퍼널 이벤트 이름입니다."),

    BAD_REQUEST(400, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다."),

    INVALID_USER_NAME(400, "유효하지 않은 이름 형식입니다.");

    private final int statusCode;
    private final String ErrorMessage;
}
