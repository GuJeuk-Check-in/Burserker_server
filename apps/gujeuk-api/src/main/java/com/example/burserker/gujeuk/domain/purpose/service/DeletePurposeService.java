package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.organ.facade.OrganFacade;
import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeReservationRepository;
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


    @Transactional
    public void execute(Long organId, Long id) {
        organFacade.getOrganById(organId);

        Purpose purpose = purposeFacade.getPurposeByIdAndOrganId(id, organId);
        int purposeIndex = purpose.getPurposeIndex();

        purposeReservationRepository.deleteAllByPurposeId(purpose.getId());
        purposeRepository.delete(purpose);

        List<Purpose> purposes = purposeRepository.findAllByOrganIdAndPurposeIndexGreaterThan(organId, purposeIndex);

        for (Purpose p : purposes) {
            p.updatePurposeIndex(p.getPurposeIndex() - 1);
        }
    }
}
