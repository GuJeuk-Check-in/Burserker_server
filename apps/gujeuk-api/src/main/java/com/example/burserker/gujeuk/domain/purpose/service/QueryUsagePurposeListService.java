package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.UsagePurposeListResponse;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.UsagePurposeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryUsagePurposeListService {
    private final PurposeRepository purposeRepository;

    @Transactional(readOnly = true)
    public UsagePurposeListResponse execute() {
        return new UsagePurposeListResponse(
                purposeRepository.findAllByOrderByPurposeIndexAsc().stream()
                        .map(UsagePurposeResponse::from)
                        .toList()
        );
    }
}
