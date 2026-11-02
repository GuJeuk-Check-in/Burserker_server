package com.example.burserker.beopdong.domain.purpose.facade;

import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.purpose.exception.PurposeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurposeFacade {
    private final PurposeRepository purposeRepository;

    public Purpose getPurposeById(Long purposeId) {
        return purposeRepository.findById(purposeId)
                .orElseThrow(() -> PurposeNotFoundException.EXCEPTION);
    }
}
