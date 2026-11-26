package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.organ.domain.Organ;
import com.example.burserker.gujeuk.domain.organ.facade.OrganFacade;
import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.SubPurpose;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationStatus;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.SubPurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.exception.SubPurposeAlreadyExistException;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.SubPurposeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSubPurposeService {
    private final PurposeRepository purposeRepository;
    private final SubPurposeRepository subPurposeRepository;
    private final OrganFacade organFacade;

    @Transactional
    public void execute(Long organId, SubPurposeRequest request) {
        Organ organ = organFacade.getOrganById(organId);

        Purpose purpose = purposeRepository.findByOrganIdAndPurposeName(organId, request.purpose())
                .orElseGet(() -> createPurpose(organ, request.purpose()));

        purpose.changeToSubInclude();

        if (subPurposeRepository.existsByPurposeIdAndSubPurposeName(purpose.getId(), request.subPurpose())) {
            throw SubPurposeAlreadyExistException.EXCEPTION;
        }

        int subPurposeIndex = subPurposeRepository.findMaxSubPurposeIndexByPurposeId(purpose.getId()) + 1;

        subPurposeRepository.save(
                SubPurpose.builder()
                        .purpose(purpose)
                        .subPurposeName(request.subPurpose())
                        .subPurposeIndex(subPurposeIndex)
                        .reservationStatus(request.subPurposeReservation())
                        .build()
        );
    }

    private Purpose createPurpose(Organ organ, String purposeName) {
        int purposeIndex = purposeRepository.findMaxPurposeIndexByOrganId(organ.getId()) + 1;

        return purposeRepository.save(
                Purpose.builder()
                        .purposeName(purposeName)
                        .purposeIndex(purposeIndex)
                        .reservationStatus(ReservationStatus.SUB_INCLUDE)
                        .organ(organ)
                        .build()
        );
    }
}
