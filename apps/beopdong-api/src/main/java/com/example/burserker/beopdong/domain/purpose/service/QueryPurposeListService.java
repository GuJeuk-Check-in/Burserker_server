package com.example.burserker.beopdong.domain.purpose.service;

import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.purpose.presentation.dto.response.PurposeListResponse;
import com.example.burserker.beopdong.domain.purpose.presentation.dto.response.PurposeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryPurposeListService {
    private final PurposeRepository purposeRepository;

    @Transactional(readOnly = true)
    public List<PurposeResponse> execute() {
        return purposeRepository.findAllByOrderByPurposeIndexAsc().stream()
                .map(PurposeResponse::from)
                .toList();
    }
}
