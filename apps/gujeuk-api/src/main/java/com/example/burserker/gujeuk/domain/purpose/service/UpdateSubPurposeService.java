package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.organ.facade.OrganFacade;
import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.SubPurpose;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.SubPurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.exception.SubPurposeAlreadyExistException;
import com.example.burserker.gujeuk.domain.purpose.exception.SubPurposeNotFoundException;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.SubPurposeUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateSubPurposeService {
    private final PurposeRepository purposeRepository;
    private final SubPurposeRepository subPurposeRepository;
    private final OrganFacade organFacade;

    @Transactional
    public void execute(Long organId, Long subPurposeId, SubPurposeUpdateRequest request) {
        // 잠금 조회를 첫 쿼리로 두어, 이후 일반 조회(중복 검사)가 잠금 획득 뒤의 스냅샷을 보도록 한다.
        // 없는 ID이거나 다른 기관의 하위 방문목적이면 조회 결과가 비어 404가 된다.
        Purpose purpose = purposeRepository.findWithLockBySubPurposeIdAndOrganId(subPurposeId, organId)
                .orElseThrow(() -> SubPurposeNotFoundException.EXCEPTION);

        organFacade.getOrganById(organId);

        SubPurpose subPurpose = subPurposeRepository.findById(subPurposeId)
                .orElseThrow(() -> SubPurposeNotFoundException.EXCEPTION);

        // 자기 자신은 제외하고, 같은 상위 방문목적 안에서만 이름 중복을 검사한다
        if (subPurposeRepository.existsByPurposeIdAndSubPurposeNameAndIdNot(
                purpose.getId(), request.subPurpose(), subPurposeId)) {
            throw SubPurposeAlreadyExistException.EXCEPTION;
        }

        subPurpose.updateSubPurpose(request.subPurpose(), request.subPurposeReservation());
    }
}
