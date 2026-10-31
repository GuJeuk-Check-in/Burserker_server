package com.example.burserker.beopdong.domain.purpose.service;

import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.purpose.exception.PurposeNotFoundException;
import com.example.burserker.beopdong.domain.purpose.facade.PurposeFacade;
import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeletePurposeService {

    private final PurposeRepository purposeRepository;
    private final PurposeFacade purposeFacade;

    @Transactional
    public void execute(Long organId, Long purposeId) {
        Purpose purpose = purposeFacade.getPurposeById(purposeId);

        if (purpose.isLocked()) {
            throw new BeopdongException(
                    ErrorCode.BAD_REQUEST, "해당 이용대장은 수정하거나 삭제할 수 없습니다."
            );
        }

        int purposeIndex = purpose.getPurposeIndex();

        purposeRepository.delete(purpose);

        List<Purpose> purposes =
                purposeRepository.findAllByOrganIdAndPurposeIndexGreaterThan(
                        organId,
                        purposeIndex
                );

        for (Purpose p : purposes) {
            p.updatePurposeIndex(p.getPurposeIndex() - 1);
        }
    }
}