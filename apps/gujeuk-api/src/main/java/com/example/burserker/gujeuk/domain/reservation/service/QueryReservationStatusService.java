package com.example.burserker.gujeuk.domain.reservation.service;

import com.example.burserker.gujeuk.domain.purpose.domain.ReservationTarget;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationStatus;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationTime;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeReservationRepository;
import com.example.burserker.gujeuk.domain.purpose.facade.ReservationTargetFacade;
import com.example.burserker.gujeuk.domain.reservation.presentation.dto.response.ReservationFacilityResponse;
import com.example.burserker.gujeuk.domain.reservation.presentation.dto.response.ReservationResponse;
import com.example.burserker.gujeuk.domain.reservation.presentation.dto.response.ReservationStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueryReservationStatusService {

    private final PurposeReservationRepository purposeReservationRepository;
    private final ReservationTargetFacade reservationTargetFacade;

    @Transactional(readOnly = true)
    public ReservationStatusResponse execute(Long organId) {

        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDate today = dateTime.toLocalDate();
        LocalTime now = dateTime.toLocalTime();

        List<ReservationResponse> reservations = purposeReservationRepository
                .findAllByPurposeOrganIdAndReservationDateOrderByStartTimeAsc(organId, today)
                .stream()
                .map(ReservationResponse::from)
                .toList();

        // 예약 가능 단위: RESERVATION 상위 + SUB_INCLUDE 상위의 예약 가능 하위
        List<ReservationTarget> targets = reservationTargetFacade.getReservableTargets(organId);
        List<ReservationFacilityResponse> facilities = targets.stream()
                .map(ReservationFacilityResponse::from)
                .toList();

        Optional<ReservationTime> currentSlot = findCurrentSlot(now);

        int inUseCount = currentSlot
                .map(slot -> purposeReservationRepository
                        .countByPurposeOrganIdAndReservationDateAndStartTime(organId, today, slot))
                .orElse(0);

        int reservedTargetCount = currentSlot
                .map(slot -> purposeReservationRepository.countReservableUnitReservations(
                        organId, today, slot, ReservationStatus.RESERVATION, ReservationStatus.SUB_INCLUDE))
                .orElse(0);

        return new ReservationStatusResponse(
                inUseCount, targets.size() - reservedTargetCount, facilities, reservations);
    }

    private Optional<ReservationTime> findCurrentSlot(LocalTime now) {
        return Arrays.stream(ReservationTime.values())
                .filter(slot -> !now.isBefore(slot.getTime())
                        && now.isBefore(slot.getTime().plusHours(1)))
                .findFirst();
    }
}
