package com.example.burserker.gujeuk.domain.user.presentation.dto.request;

import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationTime;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PurposeReservationCreateRequest(
    // 기존 앱 호환용 방문목적 이름. purposeId·subPurposeId가 있으면 무시한다.
    String purpose,

    Long purposeId,

    // 하위 방문목적 단위 예약일 때만 보낸다
    Long subPurposeId,

    @NotBlank(message = "예약인의 이름을 입력해주세요.")
    @Size(max = 30, message = "이름은 30자 이하로 입력해주세요.")
    String name,

    String phone,

    @NotNull(message = "예약할 남성 동행인 수를 입력해주세요.")
    @Min(value = 0, message = "동행인 수는 0 이상이어야 합니다.")
    Integer maleCount,

    @NotNull(message = "예약할 여성 동행인 수를 입력해주세요.")
    @Min(value = 0, message = "동행인 수는 0 이상이어야 합니다.")
    Integer femaleCount,

    @NotNull(message = "예약할 시간을 선택해주세요.")
    ReservationTime startTime
) {
}
