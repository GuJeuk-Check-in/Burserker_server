package com.example.burserker.gujeuk.domain.purpose.domain;

/**
 * 예약 단위. RESERVATION 상위 방문목적이면 subPurpose가 null이고,
 * SUB_INCLUDE 상위 방문목적의 하위 방문목적이면 subPurpose를 채운다.
 */
public record ReservationTarget(
        Purpose purpose,
        SubPurpose subPurpose
) {
    public static ReservationTarget of(Purpose purpose) {
        return new ReservationTarget(purpose, null);
    }

    public static ReservationTarget of(SubPurpose subPurpose) {
        return new ReservationTarget(subPurpose.getPurpose(), subPurpose);
    }

    // DB 생성 컬럼 slot_sub_purpose_id와 같은 규칙: 하위가 없으면 0
    public Long slotSubPurposeId() {
        return subPurpose == null ? 0L : subPurpose.getId();
    }

    public PurposeReservation.PurposeReservationBuilder reservationBuilder() {
        return PurposeReservation.builder()
                .purpose(purpose)
                .subPurpose(subPurpose);
    }
}
