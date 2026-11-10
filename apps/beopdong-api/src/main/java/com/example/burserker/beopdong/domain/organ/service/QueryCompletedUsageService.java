package com.example.burserker.beopdong.domain.organ.service;

import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeLogRepository;
import com.example.burserker.beopdong.domain.log.service.CompletePurposeUsageService;
import com.example.burserker.beopdong.domain.organ.exception.InvalidCompletedUsageException;
import com.example.burserker.beopdong.domain.organ.exception.InvalidUsagePageException;
import com.example.burserker.beopdong.domain.organ.presentation.dto.response.CompletedUsagePageResponse;
import com.example.burserker.beopdong.domain.organ.presentation.dto.response.CompletedUsageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryCompletedUsageService {

    private final PurposeLogRepository purposeLogRepository;
    private final CompletePurposeUsageService completePurposeUsageService;

    /**
     * 페이지 조건을 검사하고 만료 회차를 정리한 뒤, 선택한 시설의 완료 이력을 최신순으로 조회한다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CompletedUsagePageResponse execute(int page, int size, Long purpose) {
        if (page < 0 || size != 30) {
            throw InvalidUsagePageException.EXCEPTION;
        }
        if (purpose != null && purpose < 1) {
            throw InvalidCompletedUsageException.EXCEPTION;
        }

        completePurposeUsageService.execute();

        Page<CompletedUsageResponse> result = purposeLogRepository
                .findCompletedUsages(PurposeState.SUCCESS, purpose, PageRequest.of(page, size))
                .map(CompletedUsageResponse::from);

        return CompletedUsagePageResponse.from(result);
    }
}
