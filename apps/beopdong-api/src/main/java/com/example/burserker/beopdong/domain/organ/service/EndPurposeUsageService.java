package com.example.burserker.beopdong.domain.organ.service;

import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.organ.exception.InvalidUsageEndException;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeLogRepository;
import com.example.burserker.beopdong.domain.organ.exception.PurposeUsageNotFoundException;
import com.example.burserker.beopdong.domain.purpose.domain.PurposeUsage;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class EndPurposeUsageService {

    private final PurposeUsageRepository purposeUsageRepository;
    private final PurposeLogRepository purposeLogRepository;

    @Transactional
    public void execute(Long usageId) {
        PurposeUsage usage = purposeUsageRepository.findUsageWithLogs(usageId)
                .orElseThrow(() -> PurposeUsageNotFoundException.EXCEPTION);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        if (usage.isCompleted() || !now.isBefore(usage.getEndAt())) {
            throw InvalidUsageEndException.EXCEPTION;
        }
        completeUsage(usage, now);
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void completeExpiredUsages() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        for (Long id : purposeUsageRepository.findExpiredUsageIds(now)) {
            PurposeUsage usage = purposeUsageRepository.findUsageWithLogs(id)
                    .orElseThrow(() -> PurposeUsageNotFoundException.EXCEPTION);
            if (!usage.isCompleted() && !usage.getEndAt().isAfter(now)) {
                completeUsage(usage, usage.getEndAt());
            }
        }
    }

    private void completeUsage(PurposeUsage usage, LocalDateTime endAt) {
        usage.complete(endAt);
        purposeLogRepository.save(PurposeLog.complete(usage));
    }
}
