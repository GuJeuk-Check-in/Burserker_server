package com.example.burserker.gujeuk.domain.user.service;

import com.example.burserker.gujeuk.domain.purpose.domain.ReservationTarget;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeReservationRepository;
import com.example.burserker.gujeuk.domain.purpose.facade.ReservationTargetFacade;
import com.example.burserker.gujeuk.domain.user.presentation.dto.request.PurposeReservationCreateRequest;
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
    private final ReservationTargetFacade reservationTargetFacade;
    private final UserRepository userRepository;
    private final PurposeReservationRepository reservationRepository;

    @Transactional
    public void execute(PurposeReservationCreateRequest request) {
        String phone = request.phone() == null ? "" : request.phone().replace("-", "");

        User user = phone.isBlank()
                ? userRepository.findByOrganIdAndName(GUJEUK_ORGAN_ID, request.name().trim())
                : userRepository.findByOrganIdAndPhoneWithoutHyphens(GUJEUK_ORGAN_ID, phone);

        ReservationTarget target = reservationTargetFacade.getReservableTarget(
                GUJEUK_ORGAN_ID, request.purpose(), request.purposeId(), request.subPurposeId());

        LocalDateTime now = LocalDateTime.now(KST);

        // 중복 슬롯은 uk_reservation_slot 위반으로 GlobalExceptionHandler가 409로 바꾼다
        reservationRepository.saveAndFlush(target.reservationBuilder()
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
