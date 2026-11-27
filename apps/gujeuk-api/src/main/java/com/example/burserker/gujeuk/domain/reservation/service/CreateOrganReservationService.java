package com.example.burserker.gujeuk.domain.reservation.service;


import com.example.burserker.gujeuk.domain.purpose.domain.ReservationTarget;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeReservationRepository;
import com.example.burserker.gujeuk.domain.purpose.facade.ReservationTargetFacade;
import com.example.burserker.gujeuk.domain.reservation.exception.ReservationAlreadyExistException;
import com.example.burserker.gujeuk.domain.reservation.presentation.dto.request.ReservationCreateRequest;
import com.example.burserker.gujeuk.domain.user.domain.User;
import com.example.burserker.gujeuk.domain.user.domain.repository.UserRepository;
import com.example.burserker.gujeuk.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CreateOrganReservationService {

    private final UserRepository userRepository;
    private final ReservationTargetFacade reservationTargetFacade;
    private final PurposeReservationRepository purposeReservationRepository;

    @Transactional
    public void execute(Long organId, ReservationCreateRequest request){
        String phone = request.phone().replace("-","").trim();

        User user = userRepository
                .findAllByOrganIdAndNameAndPhoneWithoutHyphens(organId, request.name().trim(), phone)
                .stream()
                .findFirst()
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);

        ReservationTarget target = reservationTargetFacade.getReservableTarget(
                organId, request.purpose(), request.purposeId(), request.subPurposeId());

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        if (purposeReservationRepository.existsByPurposeIdAndSlotSubPurposeIdAndReservationDateAndStartTime(
                target.purpose().getId(), target.slotSubPurposeId(), today, request.startTime())) {
            throw ReservationAlreadyExistException.EXCEPTION;
        }

        try {
            purposeReservationRepository.saveAndFlush(target.reservationBuilder()
                    .user(user)
                    .name(user.getName())
                    .reservationDate(today)
                    .startTime(request.startTime())
                    .representativePhone(user.getPhone())
                    .maleCount(request.maleCount())
                    .femaleCount(request.femaleCount())
                    .build());
        } catch (DataIntegrityViolationException e) {
            String message = e.getMostSpecificCause().getMessage();
            if  (message != null && message.contains("uk_reservation_slot")) {
                throw ReservationAlreadyExistException.EXCEPTION;
            }
            throw e;
        }
    }
}
