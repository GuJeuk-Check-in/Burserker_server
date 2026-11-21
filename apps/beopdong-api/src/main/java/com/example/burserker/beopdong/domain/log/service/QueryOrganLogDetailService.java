package com.example.burserker.beopdong.domain.log.service;

import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.VisitPurposeRepository;
import com.example.burserker.beopdong.domain.log.exception.LogNotFoundException;
import com.example.burserker.beopdong.domain.log.presentation.dto.response.OrganLogDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryOrganLogDetailService {
    private final LogRepository logRepository;
    private final VisitPurposeRepository visitPurposeRepository;

    @Transactional(readOnly = true)
    public OrganLogDetailResponse execute(Long logId) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> LogNotFoundException.EXCEPTION);
        List<Long> purposes = visitPurposeRepository.findAllByLogId(logId).stream()
                .map(selected -> selected.getPurpose().getId()).sorted().toList();
        return OrganLogDetailResponse.from(log, purposes);
    }
}
