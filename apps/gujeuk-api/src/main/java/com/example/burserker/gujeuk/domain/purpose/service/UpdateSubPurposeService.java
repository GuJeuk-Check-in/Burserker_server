package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.organ.facade.OrganFacade;
import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.SubPurpose;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeReservationRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.SubPurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.exception.SubPurposeAlreadyExistException;
import com.example.burserker.gujeuk.domain.purpose.exception.SubPurposeNotFoundException;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.SubPurposeUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class UpdateSubPurposeService {
    private final PurposeRepository purposeRepository;
    private final SubPurposeRepository subPurposeRepository;
    private final PurposeReservationRepository purposeReservationRepository;
    private final OrganFacade organFacade;

    @Transactional
    public void execute(Long organId, Long subPurposeId, SubPurposeUpdateRequest request) {
        // 상위 방문목적을 잠가 같은 방문목적의 하위 항목 생성·수정·삭제와 직렬화한다.
        // 없는 ID이거나 다른 기관의 하위 방문목적이면 조회 결과가 비어 404가 된다.
        Purpose purpose = purposeRepository.findWithLockBySubPurposeIdAndOrganId(subPurposeId, organId)
                .orElseThrow(() -> SubPurposeNotFoundException.EXCEPTION);

        organFacade.getOrganById(organId);

        // 잠금 대기 중 다른 트랜잭션이 커밋한 변경(삭제 등)을 보기 위해 잠금 읽기로 다시 조회한다
        SubPurpose subPurpose = subPurposeRepository.findWithLockById(subPurposeId)
                .orElseThrow(() -> SubPurposeNotFoundException.EXCEPTION);

        // 자기 자신은 제외하고, 같은 상위 방문목적 안에서만 이름 중복을 검사한다 (잠금 읽기로 최신 상태 확인)
        if (subPurposeRepository.findWithLockByPurposeIdAndSubPurposeNameAndIdNot(
                purpose.getId(), request.subPurpose(), subPurposeId).isPresent()) {
            throw SubPurposeAlreadyExistException.EXCEPTION;
        }

        // 예약 가능 → 예약 불가: 오늘 이후 이 하위 방문목적의 예약을 정리한다
        if (subPurpose.isReservable() && !request.subPurposeReservation()) {
            purposeReservationRepository.deleteSubPurposeUnitReservationsFrom(
                    subPurpose.getId(), LocalDate.now(ZoneId.of("Asia/Seoul")));
        }

        subPurpose.updateSubPurpose(request.subPurpose(), request.subPurposeReservation());
    }
}
