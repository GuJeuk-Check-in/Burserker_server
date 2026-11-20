package com.example.burserker.beopdong.domain.log.service;

import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.VisitPurpose;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.VisitPurposeRepository;
import com.example.burserker.beopdong.domain.log.presentation.dto.response.OrganLogListResponse;
import com.example.burserker.beopdong.domain.log.exception.InvalidLogPageException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryOrganLogListService {
    private final LogRepository logRepository;
    private final VisitPurposeRepository visitPurposeRepository;

    @Transactional(readOnly = true)
    public OrganLogListResponse execute(int page, int size) {
        if (page < 0 || size != 30) {
            throw InvalidLogPageException.EXCEPTION;
        }
        Page<Log> logs = logRepository.findOrganLogs(PageRequest.of(page, size));
        List<Long> logIds = logs.getContent().stream().map(Log::getId).toList();
        List<VisitPurpose> selected = logIds.isEmpty() ? List.of() : visitPurposeRepository.findAllByLogIdIn(logIds);
        List<OrganLogListResponse.LogPreview> content = logs.getContent().stream().map(log -> {
            List<Long> purposes = selected.stream()
                    .filter(purpose -> purpose.getLog().getId().equals(log.getId()))
                    .map(purpose -> purpose.getPurpose().getId()).sorted().toList();
            return new OrganLogListResponse.LogPreview(log.getId(), log.getName(), log.getVisitAt().toLocalDate(), purposes);
        }).toList();
        return new OrganLogListResponse(content, new OrganLogListResponse.PageableResponse(page, size),
                logs.getTotalElements(), logs.getTotalPages(), logs.isLast());
    }
}
