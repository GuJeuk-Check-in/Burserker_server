package com.example.burserker.beopdong.domain.log.service;

import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.exception.LogCheckedOutException;
import com.example.burserker.beopdong.domain.log.domain.VisitPurpose;
import com.example.burserker.beopdong.domain.log.exception.LogDetailNotFoundException;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.VisitPurposeRepository;
import com.example.burserker.beopdong.domain.log.presentation.dto.response.LogDetailResponse;
import com.example.burserker.beopdong.domain.purpose.domain.PurposeUsage;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryLogDetailService {
    private final LogRepository logRepository;
    private final VisitPurposeRepository visitPurposeRepository;
    private final PurposeUsageRepository purposeUsageRepository;

    @Transactional(readOnly = true)
    public LogDetailResponse execute(Long logId) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> LogDetailNotFoundException.EXCEPTION);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        if (log.isCheckedOut()) {
            throw LogCheckedOutException.EXCEPTION;
        }
        List<VisitPurpose> selected = visitPurposeRepository.findAllByLogId(logId);
        List<PurposeUsage> usages = purposeUsageRepository.findUsagesByLogId(logId);
        List<LogDetailResponse.PurposeResponse> purposes = List.of(
                toPurposeResponse("pcForm", "인터넷", selected, usages, now),
                toPurposeResponse("pingForm", "탁구", selected, usages, now),
                toPurposeResponse("singForm", "노래방", selected, usages, now)
        );
        return new LogDetailResponse(log.getName(), purposes, log.getOrganRemainTime(now));
    }

    private LogDetailResponse.PurposeResponse toPurposeResponse(String form, String name, List<VisitPurpose> selected, List<PurposeUsage> usages, LocalDateTime now) {
        boolean purposeSelect = selected.stream().anyMatch(purpose -> name.equals(purpose.getPurpose().getPurposeName()));
        String state = "none";
        long remainTime = 0;
        for (PurposeUsage usage : usages) {
            if (name.equals(usage.getPurpose().getPurposeName())) {
                state = usage.isCompleted() ? "success" : "pending";
                if (!usage.isCompleted() && usage.getEndAt().isAfter(now)) {
                    Duration remaining = Duration.between(now, usage.getEndAt());
                    long minutes = remaining.toMinutes();
                    remainTime = minutes + (remaining.compareTo(Duration.ofMinutes(minutes)) > 0 ? 1 : 0);
                }
                break;
            }
        }
        return new LogDetailResponse.PurposeResponse(form, purposeSelect, state, remainTime);
    }
}
