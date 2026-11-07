package com.example.burserker.beopdong.domain.log.service;

import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompletePurposeUsageService {

    private final PurposeLogRepository purposeLogRepository;

    @Transactional
    public void execute() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        List<PurposeLog> expiredUsages = purposeLogRepository
                .findAllByPurposeStateAndEndAtLessThanEqual(PurposeState.PENDING, now);

        for (PurposeLog purposeLog : expiredUsages) {
            purposeLog.completeIfExpired(now);
        }
    }
}