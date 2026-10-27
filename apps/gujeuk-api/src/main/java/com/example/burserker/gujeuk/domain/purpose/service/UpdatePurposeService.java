package com.example.burserker.gujeuk.domain.purpose.service;

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

    @Transactional
    public void execute(Long id, PurposeRequest purposeRequest) {
        Purpose purpose = purposeFacade.getPurposeById(id);

        purpose.updatePurpose(purposeRequest.purpose(), purposeRequest.reservationStatus());
    }
}
