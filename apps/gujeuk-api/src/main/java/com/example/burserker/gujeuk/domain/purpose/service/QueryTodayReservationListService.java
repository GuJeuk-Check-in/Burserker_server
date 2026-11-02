package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeReservationRepository;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.TodayReservationListResponse;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.TodayReservationResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryTodayReservationListService {
    private final PurposeReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public TodayReservationListResponse execute(Long organId) {
        return new TodayReservationListResponse(
                reservationRepository.findAllByPurposeOrganIdAndReservationDateOrderByStartTimeAscIdAsc(
                                organId, LocalDate.now(ZoneId.of("Asia/Seoul"))).stream()
                        .map(TodayReservationResponse::from)
                        .toList()
        );
    }
}
