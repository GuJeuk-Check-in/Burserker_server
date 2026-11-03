package com.example.burserker.gujeuk.domain.reservation.service;

import com.example.burserker.gujeuk.domain.purpose.domain.PurposeReservation;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeReservationRepository;
import com.example.burserker.gujeuk.domain.reservation.exception.ReservationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class DeleteReservationService {

    private final PurposeReservationRepository purposeReservationRepository;

    @Transactional
    public void execute(Long organId, Long reservationId) {
        PurposeReservation reservation = purposeReservationRepository
                .findByIdAndPurposeOrganId(reservationId, organId)
                .orElseThrow(() -> ReservationNotFoundException.EXCEPTION);

        purposeReservationRepository.delete(reservation);
    }
}
