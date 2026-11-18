package com.example.burserker.beopdong.domain.check_in.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PcFormRequest(
        @NotNull(message = "빈칸을 채워주세요.")
        Long logId,

        @NotNull(message = "빈칸을 채워주세요.")
        @Min(value = 1, message = "좌석 번호를 1 ~ 10사이로 입력해주세요.")
        @Max(value = 10, message = "좌석 번호를 1 ~ 10사이로 입력해주세요.")
        Integer seat,

        @NotBlank(message = "빈칸을 채워주세요.")
        String usePurpose
) {
}