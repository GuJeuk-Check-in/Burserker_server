package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.organ.facade.OrganFacade;
import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.SubPurpose;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.SubPurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.exception.SubPurposeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeleteSubPurposeService {
    private final PurposeRepository purposeRepository;
    private final SubPurposeRepository subPurposeRepository;
    private final OrganFacade organFacade;

    @Transactional
    public void execute(Long organId, Long subPurposeId) {
        // 상위 방문목적을 잠가 같은 방문목적의 하위 항목 생성·수정·삭제와 직렬화한다.
        // 없는 ID이거나 다른 기관의 하위 방문목적이면 조회 결과가 비어 404가 된다.
        Purpose purpose = purposeRepository.findWithLockBySubPurposeIdAndOrganId(subPurposeId, organId)
                .orElseThrow(() -> SubPurposeNotFoundException.EXCEPTION);

        organFacade.getOrganById(organId);

        // 잠금 대기 중 다른 트랜잭션이 커밋한 변경을 보기 위해 하위 방문목적 전체를 잠금 읽기로 조회한다
        List<SubPurpose> subPurposes = subPurposeRepository.findAllWithLockByPurposeId(purpose.getId());

        SubPurpose subPurpose = subPurposes.stream()
                .filter(s -> s.getId().equals(subPurposeId))
                .findFirst()
                .orElseThrow(() -> SubPurposeNotFoundException.EXCEPTION);
        int subPurposeIndex = subPurpose.getSubPurposeIndex();

        subPurposeRepository.delete(subPurpose);

        for (SubPurpose s : subPurposes) {
            if (s.getSubPurposeIndex() > subPurposeIndex) {
                s.updateSubPurposeIndex(s.getSubPurposeIndex() - 1);
            }
        }

        // 마지막 하위 방문목적까지 지워졌으면 상위 방문목적 상태를 NONE으로 되돌린다
        if (subPurposes.size() == 1) {
            purpose.changeToNone();
        }
    }
}
