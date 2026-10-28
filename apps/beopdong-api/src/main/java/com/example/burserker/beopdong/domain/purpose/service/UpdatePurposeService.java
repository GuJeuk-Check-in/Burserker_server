package com.example.burserker.beopdong.domain.purpose.service;

import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.facade.PurposeFacade;
import com.example.burserker.beopdong.domain.purpose.presentation.dto.request.PurposeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePurposeService {
    private final PurposeFacade purposeFacade;

    @Transactional
    public void execute(Long purposeId, PurposeRequest request) {
        Purpose purpose = purposeFacade.getPurposeById(purposeId);

        purpose.updatePurpose(request.purposeType());
    }
}
