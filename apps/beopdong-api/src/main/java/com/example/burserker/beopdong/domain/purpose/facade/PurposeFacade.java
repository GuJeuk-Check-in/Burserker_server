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

    public Purpose getPurposeById(Long purposeId) {
        return purposeRepository.findById(purposeId)
                .orElseThrow(() -> PurposeNotFoundException.EXCEPTION);
    }

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
