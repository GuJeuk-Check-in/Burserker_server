package com.example.burserker.gujeuk.domain.reservation.service;


import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.PurposeReservation;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeReservationRepository;
import com.example.burserker.gujeuk.domain.purpose.exception.PurposeNotFoundException;
import com.example.burserker.gujeuk.domain.purpose.exception.ReservationNotAllowedException;
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
    private final PurposeRepository purposeRepository;
    private final PurposeReservationRepository purposeReservationRepository;

    @Transactional
    public void execute(Long organId, ReservationCreateRequest request){
        String phone = request.phone().replace("-","").trim();

        User user = userRepository
                .findAllByOrganIdAndNameAndPhoneWithoutHyphens(organId, request.name().trim(), phone)
                .stream()
                .findFirst()
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);

        Purpose purpose = purposeRepository
                .findByOrganIdAndPurposeName(organId, request.purpose().trim())
                .orElseThrow(() -> PurposeNotFoundException.EXCEPTION);

        if(!purpose.isReservationStatus()){
            throw ReservationNotAllowedException.EXCEPTION;
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        if (purposeReservationRepository.existsByPurposeIdAndReservationDateAndStartTime(
                purpose.getId(), today, request.startTime())) {
            throw ReservationAlreadyExistException.EXCEPTION;
        }

        try {
            purposeReservationRepository.saveAndFlush(PurposeReservation.builder()
                    .purpose(purpose)
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
