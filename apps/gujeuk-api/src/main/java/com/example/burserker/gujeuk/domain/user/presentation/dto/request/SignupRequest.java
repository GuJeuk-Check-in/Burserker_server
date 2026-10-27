package com.example.burserker.gujeuk.domain.user.presentation.dto.request;

import com.example.burserker.gujeuk.domain.purpose.enums.ReservationTime;
import com.example.burserker.gujeuk.domain.user.domain.enums.Gender;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record SignupRequest(
        @NotBlank(message = "이름을 입력해주세요.")
        @Size(max = 30, message = "이름은 30자 이하로 입력해주세요.")
        String name,

        @NotNull(message = "성별을 입력해주세요.")
        Gender gender,

        @NotBlank(message = "전화번호를 입력해주세요.")
        String phone,

        @NotNull(message = "남자 동행인 수는 null일 수 없습니다.")
        @PositiveOrZero(message = "남자 동행인 수는 0 이상이어야 합니다.")
        Integer maleCount,

        @NotNull(message = "여자 동행인 수는 null일 수 없습니다.")
        @PositiveOrZero(message = "여자 동행인 수는 0 이상이어야 합니다.")
        Integer femaleCount,

        @NotBlank(message = "생년월일을 입력해주세요.")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "생년월일은 yyyy-MM-dd 형식이어야 합니다.")
        String birthYMD,

        @NotBlank(message = "거주지를 입력해주세요.")
        String residence,

        @NotNull(message = "개인정보 동의 여부를 입력해주세요.")
        @AssertTrue(message = "개인정보 수집 및 이용에 동의해주세요.")
        Boolean privacyAgreed,

        @NotBlank(message = "방문목적을 입력해주세요.")
        String purpose,

        @NotNull(message = "방문시각을 비워둘 수 없습니다.")
        LocalDateTime visitTime,

        ReservationTime reservationTime
) {
}
