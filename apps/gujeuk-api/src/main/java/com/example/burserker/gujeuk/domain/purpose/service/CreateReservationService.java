package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.PurposeReservation;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeReservationRepository;
import com.example.burserker.gujeuk.domain.purpose.exception.PurposeNotFoundException;
import com.example.burserker.gujeuk.domain.purpose.exception.ReservationNotAllowedException;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.PurposeReservationCreateRequest;
import com.example.burserker.gujeuk.domain.user.domain.User;
import com.example.burserker.gujeuk.domain.user.domain.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateReservationService {
    private static final Long GUJEUK_ORGAN_ID = 1L;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final PurposeRepository purposeRepository;
    private final UserRepository userRepository;
    private final PurposeReservationRepository reservationRepository;

    @Transactional
    public void execute(PurposeReservationCreateRequest request) {
        String phone = request.phone() == null ? "" : request.phone().replace("-", "");

        User user = phone.isBlank()
                ? userRepository.findByOrganIdAndName(GUJEUK_ORGAN_ID, request.name().trim())
                : userRepository.findByOrganIdAndPhoneWithoutHyphens(GUJEUK_ORGAN_ID, phone);

        Purpose purpose = purposeRepository.findByOrganIdAndPurposeName(GUJEUK_ORGAN_ID, request.purpose().trim())
                .orElseThrow(() -> PurposeNotFoundException.EXCEPTION);

        if (!purpose.isReservationStatus()) {
            throw ReservationNotAllowedException.EXCEPTION;
        }

        LocalDateTime now = LocalDateTime.now(KST);

        reservationRepository.saveAndFlush(PurposeReservation.builder()
                .purpose(purpose)
                .user(user)
                .name(user.getName())
                .reservationDate(now.toLocalDate())
                .startTime(request.startTime())
                .representativePhone(user.getPhone())
                .maleCount(request.maleCount())
                .femaleCount(request.femaleCount())
                .build());
    }
}
