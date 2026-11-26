package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.organ.facade.OrganFacade;
import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeReservationRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.SubPurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.facade.PurposeFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeletePurposeService {
    private final PurposeRepository purposeRepository;
    private final PurposeFacade purposeFacade;
    private final OrganFacade organFacade;
    private final PurposeReservationRepository purposeReservationRepository;
    private final SubPurposeRepository subPurposeRepository;

    @Transactional
    public void execute(Long organId, Long id) {
        // 잠금 조회를 첫 쿼리로 두어, 이후 일반 조회가 잠금 획득 뒤의 스냅샷을 보도록 한다.
        Purpose purpose = purposeFacade.getPurposeWithLock(id, organId);

        organFacade.getOrganById(organId);
        int purposeIndex = purpose.getPurposeIndex();

        purposeReservationRepository.deleteAllByPurposeId(purpose.getId());
        subPurposeRepository.deleteAllByPurposeId(purpose.getId());
        purposeRepository.delete(purpose);

        List<Purpose> purposes = purposeRepository.findAllByOrganIdAndPurposeIndexGreaterThan(organId, purposeIndex);

        for (Purpose p : purposes) {
            p.updatePurposeIndex(p.getPurposeIndex() - 1);
        }
    }
}
