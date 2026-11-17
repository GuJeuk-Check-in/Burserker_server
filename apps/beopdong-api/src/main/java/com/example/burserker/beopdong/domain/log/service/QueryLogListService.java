package com.example.burserker.beopdong.domain.log.service;

import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.VisitPurpose;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.VisitPurposeRepository;
import com.example.burserker.beopdong.domain.log.presentation.dto.response.LogListResponse;
import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryLogListService {
    private final LogRepository logRepository;
    private final VisitPurposeRepository visitPurposeRepository;

    @Transactional(readOnly = true)
    public LogListResponse execute(int page, int size) {
        if (page < 0 || size != 30) {
            throw new BeopdongException(ErrorCode.BAD_REQUEST, "page 또는 size 값이 올바르지 않음");
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        List<Log> logs = logRepository.findLogs(PageRequest.of(page, size));
        List<Long> logIds = logs.stream().map(Log::getId).toList();
        List<VisitPurpose> purposes = logIds.isEmpty() ? List.of() : visitPurposeRepository.findAllByLogIdIn(logIds);
        List<LogListResponse.LogPreview> content = logs.stream().map(log -> {
            List<Long> selected = purposes.stream()
                    .filter(purpose -> purpose.getLog().getId().equals(log.getId()))
                    .map(purpose -> purpose.getPurpose().getId()).sorted().toList();
            return new LogListResponse.LogPreview(log.getId(), log.getName(), selected, log.getOrganRemainTime(now));
        }).toList();
        return new LogListResponse(content, new LogListResponse.PageableResponse(page, size));
    }
}
