package com.example.burserker.beopdong.domain.purpose.service;

import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.facade.PurposeFacade;
import com.example.burserker.beopdong.domain.purpose.presentation.dto.request.PurposeRequest;
import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.burserker.beopdong.domain.purpose.exception.PurposeNotModifiableException;

@Service
@RequiredArgsConstructor
public class UpdatePurposeService {
    private final PurposeFacade purposeFacade;

    @Transactional
    public void execute(Long purposeId, PurposeRequest request) {
        Purpose purpose = purposeFacade.getPurposeById(purposeId);

        if (purpose.isLocked()) {
            throw PurposeNotModifiableException.EXCEPTION;
        }

        purpose.updatePurpose(request.purposeName());
    }
}
