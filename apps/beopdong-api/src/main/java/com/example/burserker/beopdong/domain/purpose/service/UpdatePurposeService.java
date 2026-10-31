package com.example.burserker.beopdong.domain.purpose.service;

import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.facade.PurposeFacade;
import com.example.burserker.beopdong.domain.purpose.presentation.dto.request.PurposeRequest;
import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
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

        if (purpose.isLocked()) {
            throw new BeopdongException(
                    ErrorCode.BAD_REQUEST, "해당 이용대장은 수정하거나 삭제할 수 없습니다."
            );
        }

        purpose.updatePurpose(request.purposeName());
    }
}
