package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.purpose.domain.repository.ReservationStatusRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationTime;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.ReservationResponse;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.ReservationStatusResponse;
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

    private final ReservationStatusRepository reservationStatusRepository;

    @Transactional(readOnly = true)
    public ReservationStatusResponse execute(Long organId) {

        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDate today = dateTime.toLocalDate();
        LocalTime now = dateTime.toLocalTime();

        List<ReservationResponse> reservations = reservationStatusRepository
                .findAllByPurposeOrganIdAndReservationDateOrderByStartTimeAsc(organId, today)
                .stream()
                .map(ReservationResponse::from)
                .toList();

        int inUseCount = findCurrentSlot(now)
                .map(slot -> reservationStatusRepository
                        .countByPurposeOrganIdAndReservationDateAndStartTimeAndPurposeReservationStatusTrue(
                                organId, today, slot))
                .orElse(0);

        int totalCount = reservationStatusRepository.countReservationRequiredPurpose(organId);

        return new ReservationStatusResponse(inUseCount, totalCount - inUseCount, reservations);
    }

    private Optional<ReservationTime> findCurrentSlot(LocalTime now) {
        return Arrays.stream(ReservationTime.values())
                .filter(slot -> !now.isBefore(slot.getTime())
                        && now.isBefore(slot.getTime().plusHours(1)))
                .findFirst();
    }
}