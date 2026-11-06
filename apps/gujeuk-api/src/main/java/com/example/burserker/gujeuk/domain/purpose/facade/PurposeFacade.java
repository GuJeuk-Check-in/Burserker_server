package com.example.burserker.gujeuk.domain.purpose.facade;

import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.exception.PurposeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurposeFacade {
    private final PurposeRepository purposeRepository;

    public Purpose getPurpose(Long organId, String purposeName) {
        return purposeRepository.findByOrganIdAndPurposeName(organId, purposeName)
                .orElseThrow(() -> PurposeNotFoundException.EXCEPTION);
    }

    public Purpose getPurposeById(Long purposeId) {
        return purposeRepository.findById(purposeId)
                .orElseThrow(() -> PurposeNotFoundException.EXCEPTION);
    }

    public Purpose getPurposeByIdAndOrganId(Long purposeId, Long organId) {
        return purposeRepository.findByIdAndOrganId(purposeId, organId)
                .orElseThrow(() -> PurposeNotFoundException.EXCEPTION);
    }
}
