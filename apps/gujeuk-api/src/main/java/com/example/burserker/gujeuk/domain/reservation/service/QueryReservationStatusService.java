package com.example.burserker.gujeuk.domain.reservation.service;

import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeReservationRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationTime;
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
    private final PurposeRepository purposeRepository;

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


        Optional<ReservationTime> currentSlot = findCurrentSlot(now);

        int inUseCount = currentSlot
                .map(slot -> purposeReservationRepository
                        .countByPurposeOrganIdAndReservationDateAndStartTime(organId, today, slot))
                .orElse(0);


        int reservedRequiredCount = currentSlot
                .map(slot -> purposeReservationRepository
                        .countByPurposeOrganIdAndReservationDateAndStartTimeAndPurposeReservationStatusTrue(
                                organId, today, slot))
                .orElse(0);

        int requiredCount = purposeRepository.countByOrganIdAndReservationStatusTrue(organId);

        return new ReservationStatusResponse(inUseCount, requiredCount - reservedRequiredCount, reservations);
    }

    private Optional<ReservationTime> findCurrentSlot(LocalTime now) {
        return Arrays.stream(ReservationTime.values())
                .filter(slot -> !now.isBefore(slot.getTime())
                        && now.isBefore(slot.getTime().plusHours(1)))
                .findFirst();
    }
}