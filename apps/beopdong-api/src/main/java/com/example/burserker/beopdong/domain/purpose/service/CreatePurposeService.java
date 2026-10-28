package com.example.burserker.beopdong.domain.purpose.service;

import com.example.burserker.beopdong.domain.organ.domain.Organ;
import com.example.burserker.beopdong.domain.organ.facade.OrganFacade;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.purpose.presentation.dto.request.PurposeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePurposeService {

    private final PurposeRepository purposeRepository;
    private final OrganFacade organFacade;

    @Transactional
    public void execute(Long organId, PurposeRequest request) {
        Organ organ = organFacade.getOrganById(organId);

        int purposeIndex =
                purposeRepository.findMaxPurposeIndexByOrganId(organId) + 1;

        purposeRepository.save(
                Purpose.builder()
                        .purposeName(request.purposeType())
                        .purposeIndex(purposeIndex)
                        .organ(organ)
                        .build()
        );
    }
}