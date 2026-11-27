package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.organ.facade.OrganFacade;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationStatus;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeReservationRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.SubPurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.exception.PurposeStatusFixedException;
import com.example.burserker.gujeuk.domain.purpose.exception.SubIncludeNotAllowedException;
import com.example.burserker.gujeuk.domain.purpose.facade.PurposeFacade;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.PurposeRequest;
import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class UpdatePurposeService {
    private final PurposeFacade purposeFacade;
    private final OrganFacade organFacade;
    private final SubPurposeRepository subPurposeRepository;
    private final PurposeReservationRepository purposeReservationRepository;

    @Transactional
    public void execute(Long organId, Long id, PurposeRequest purposeRequest) {
        // 잠금 조회를 첫 쿼리로 두어, 이후 일반 조회가 잠금 획득 뒤의 스냅샷을 보도록 한다.
        Purpose purpose = purposeFacade.getPurposeWithLock(id, organId);

        organFacade.getOrganById(organId);

        boolean hasSubPurpose = subPurposeRepository.existsByPurposeId(purpose.getId());
        boolean requestSubInclude = purposeRequest.reservationStatus() == ReservationStatus.SUB_INCLUDE;

        if (hasSubPurpose && !requestSubInclude) {
            throw PurposeStatusFixedException.EXCEPTION;
        }

        if (!hasSubPurpose && requestSubInclude) {
            throw SubIncludeNotAllowedException.EXCEPTION;
        }

        // RESERVATION → NONE: 예약할 수 없게 되므로 오늘 이후 상위 단위 예약을 정리한다
        if (purpose.isReservable() && purposeRequest.reservationStatus() != ReservationStatus.RESERVATION) {
            purposeReservationRepository.deletePurposeUnitReservationsFrom(
                    purpose.getId(), LocalDate.now(ZoneId.of("Asia/Seoul")));
        }

        purpose.updatePurpose(purposeRequest.purpose(), purposeRequest.reservationStatus());
    }
}
