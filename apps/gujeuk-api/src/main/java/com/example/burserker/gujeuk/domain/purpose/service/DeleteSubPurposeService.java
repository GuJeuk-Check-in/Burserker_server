package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.organ.facade.OrganFacade;
import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.SubPurpose;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.SubPurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.exception.SubPurposeNotFoundException;
import com.example.burserker.gujeuk.domain.purpose.facade.PurposeFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeleteSubPurposeService {
    private final SubPurposeRepository subPurposeRepository;
    private final PurposeFacade purposeFacade;
    private final OrganFacade organFacade;

    @Transactional
    public void execute(Long organId, Long subPurposeId) {
        organFacade.getOrganById(organId);

        Long purposeId = subPurposeRepository.findPurposeIdByIdAndOrganId(subPurposeId, organId)
                .orElseThrow(() -> SubPurposeNotFoundException.EXCEPTION);

        // 상위 방문목적에 락을 먼저 걸어 같은 방문목적의 하위 항목 순서가 동시에 바뀌지 않게 한다
        Purpose purpose = purposeFacade.getPurposeWithLock(purposeId, organId);

        SubPurpose subPurpose = subPurposeRepository.findById(subPurposeId)
                .orElseThrow(() -> SubPurposeNotFoundException.EXCEPTION);
        int subPurposeIndex = subPurpose.getSubPurposeIndex();

        subPurposeRepository.delete(subPurpose);

        List<SubPurpose> subPurposes =
                subPurposeRepository.findAllByPurposeIdAndSubPurposeIndexGreaterThan(purposeId, subPurposeIndex);

        for (SubPurpose s : subPurposes) {
            s.updateSubPurposeIndex(s.getSubPurposeIndex() - 1);
        }

        // 마지막 하위 방문목적까지 지워졌으면 상위 방문목적 상태를 NONE으로 되돌린다
        if (!subPurposeRepository.existsByPurposeId(purposeId)) {
            purpose.changeToNone();
        }
    }
}
