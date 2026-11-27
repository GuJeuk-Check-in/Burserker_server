package com.example.burserker.gujeuk.domain.user.service;

import com.example.burserker.gujeuk.domain.purpose.domain.ReservationTarget;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationTime;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeReservationRepository;
import com.example.burserker.gujeuk.domain.purpose.facade.ReservationTargetFacade;
import com.example.burserker.gujeuk.domain.user.presentation.dto.response.AvailableReservationTimeListResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryAvailableReservationTimeListService {
    private static final Long GUJEUK_ORGAN_ID = 1L;
    private final ReservationTargetFacade reservationTargetFacade;
    private final PurposeReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public AvailableReservationTimeListResponse execute(String purposeName, Long purposeId, Long subPurposeId) {
        ReservationTarget target = reservationTargetFacade.getReservableTarget(
                GUJEUK_ORGAN_ID, purposeName, purposeId, subPurposeId);

        EnumSet<ReservationTime> availableTimes = EnumSet.allOf(ReservationTime.class);
        availableTimes.removeAll(reservationRepository.findReservedTimes(
                target.purpose().getId(), target.slotSubPurposeId(), LocalDate.now(ZoneId.of("Asia/Seoul"))));

        return new AvailableReservationTimeListResponse(List.copyOf(availableTimes));
    }
}
