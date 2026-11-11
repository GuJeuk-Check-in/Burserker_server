package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.organ.facade.OrganFacade;
import com.example.burserker.gujeuk.domain.purpose.facade.PurposeFacade;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.PurposeRequest;
import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePurposeService {
    private final PurposeFacade purposeFacade;
    private final OrganFacade organFacade;

    @Transactional
    public void execute(Long organId, Long id, PurposeRequest purposeRequest) {
        organFacade.getOrganById(organId);

        Purpose purpose = purposeFacade.getPurposeByIdAndOrganId(id, organId);

        purpose.updatePurpose(purposeRequest.purpose(), purposeRequest.reservationStatus());
    }
}
