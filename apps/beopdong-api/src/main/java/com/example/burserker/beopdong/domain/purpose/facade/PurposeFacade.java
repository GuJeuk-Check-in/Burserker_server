package com.example.burserker.beopdong.domain.purpose.facade;

import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.purpose.exception.PurposeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PurposeFacade {
    private final PurposeRepository purposeRepository;

    /**
     * 이용목적을 조회하고 없으면 전용 예외를 발생시킨다.
     */
    public Purpose getPurposeById(Long purposeId) {
        return purposeRepository.findById(purposeId)
                .orElseThrow(() -> PurposeNotFoundException.EXCEPTION);
    }

    /**
     * 중복 ID를 제거한 선택 목록을 조회하고, 존재하지 않는 목적이 섞이면 요청을 거절한다.
     */
    public List<Purpose> getPurposesByIds(List<Long> purposeIds) {
        List<Long> uniquePurposeIds = purposeIds.stream()
                .distinct()
                .toList();

        List<Purpose> purposes =
                purposeRepository.findAllById(uniquePurposeIds);

        if (uniquePurposeIds.size() != purposes.size()) {
            throw PurposeNotFoundException.EXCEPTION;
        }

        return purposes;
    }
}
