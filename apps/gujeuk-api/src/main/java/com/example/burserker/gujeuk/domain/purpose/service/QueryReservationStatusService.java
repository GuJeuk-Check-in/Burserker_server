package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.purpose.domain.repository.ReservationStatusRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationTime;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.ReservationResponse;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.ReservationStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueryReservationStatusService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ReservationStatusRepository reservationStatusRepository;

    @Transactional(readOnly = true)
    public ReservationStatusResponse execute(Long organId) {
        LocalDate today = LocalDate.now(KST);
        LocalTime now = LocalTime.now(KST);

        List<ReservationResponse> reservations = reservationStatusRepository
                .findAllByPurposeOrganIdAndReservationDateOrderByStartTimeAsc(organId, today)
                .stream()
                .map(ReservationResponse::from)
                .toList();

        long inUseCount = findCurrentSlot(now)
                .map(slot -> reservationStatusRepository
                        .countByPurposeOrganIdAndReservationDateAndStartTimeAndPurposeReservationStatusTrue(
                                organId, today, slot))
                .orElse(0L);

        long totalCount = reservationStatusRepository.countReservationRequiredPurpose(organId);

        return new ReservationStatusResponse(inUseCount, totalCount - inUseCount, reservations);
    }

    private Optional<ReservationTime> findCurrentSlot(LocalTime now) {
        return Arrays.stream(ReservationTime.values())
                .filter(slot -> !now.isBefore(slot.getTime())
                        && now.isBefore(slot.getTime().plusHours(1)))
                .findFirst();
    }
}