package com.example.burserker.gujeuk.domain.purpose.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.LocalTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationTime {
    TIME_09_30(LocalTime.of(9, 30)),
    TIME_10_30(LocalTime.of(10, 30)),
    TIME_11_30(LocalTime.of(11, 30)),
    TIME_12_30(LocalTime.of(12, 30)),
    TIME_13_30(LocalTime.of(13, 30)),
    TIME_14_30(LocalTime.of(14, 30)),
    TIME_15_30(LocalTime.of(15, 30)),
    TIME_16_30(LocalTime.of(16, 30)),
    TIME_17_30(LocalTime.of(17, 30)),
    TIME_18_30(LocalTime.of(18, 30)),
    TIME_19_30(LocalTime.of(19, 30)),
    TIME_20_30(LocalTime.of(20, 30));

    private final LocalTime time;

    @JsonValue
    public String value() {
        return time.toString();
    }

    @JsonCreator
    public static ReservationTime fromValue(String value) {
        for (ReservationTime reservationTime : values()) {
            if (reservationTime.value().equals(value)) {
                return reservationTime;
            }
        }
        throw new IllegalArgumentException("예약할 수 없는 시간입니다: " + value);
    }
}
