package com.example.burserker.beopdong.domain.organ.service;

import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeLogRepository;
import com.example.burserker.beopdong.domain.organ.exception.InvalidCompletedUsageException;
import com.example.burserker.beopdong.domain.organ.exception.InvalidUsagePageException;
import com.example.burserker.beopdong.domain.organ.presentation.dto.response.CompletedUsagePageResponse;
import com.example.burserker.beopdong.domain.organ.presentation.dto.response.CompletedUsageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class QueryCompletedUsageService {

    private final PurposeLogRepository purposeLogRepository;

    /**
     * 페이지 조건을 검사하고 선택한 시설의 완료 이력을 최신순으로 조회한다.
     */
    @Transactional(readOnly = true)
    public CompletedUsagePageResponse execute(int page, int size, Long purpose) {
        if (page < 0 || size != 30) {
            throw InvalidUsagePageException.EXCEPTION;
        }
        if (purpose != null && purpose < 1) {
            throw InvalidCompletedUsageException.EXCEPTION;
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        Page<CompletedUsageResponse> result = purposeLogRepository
                .findCompletedUsages(PurposeState.SUCCESS, PurposeState.PENDING, purpose, now, PageRequest.of(page, size))
                .map(CompletedUsageResponse::from);

        return CompletedUsagePageResponse.from(result);
    }
}
