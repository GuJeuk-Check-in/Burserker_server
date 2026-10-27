package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.organ.facade.OrganFacade;
import com.example.burserker.gujeuk.domain.purpose.exception.PurposeAlreadyExistException;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.PurposeRequest;
import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
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
        if(purposeRepository.findByOrganIdAndPurposeName(organId, request.purpose()).isPresent()) {
            throw PurposeAlreadyExistException.EXCEPTION;
        }

        int purposeIndex = purposeRepository.findMaxPurposeIndexByOrganId(organId) + 1;

        purposeRepository.save(
                Purpose.builder()
                        .purposeName(request.purpose())
                        .purposeIndex(purposeIndex)
                        .reservationStatus(request.reservationStatus())
                        .organ(organFacade.getOrganReference(organId))
                        .build()
        );
    }
}
