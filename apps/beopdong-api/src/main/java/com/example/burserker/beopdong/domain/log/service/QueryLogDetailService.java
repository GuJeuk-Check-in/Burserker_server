package com.example.burserker.beopdong.domain.log.service;

import com.example.burserker.beopdong.domain.log.domain.Log;
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
        List<VisitPurpose> selected = visitPurposeRepository.findAllByLogId(logId);
        List<PurposeUsage> usages = purposeUsageRepository.findUsagesByLogId(logId);
        List<LogDetailResponse.PurposeResponse> purposes = List.of(
                toPurposeResponse("pcForm", "인터넷", selected, usages),
                toPurposeResponse("pingForm", "탁구", selected, usages),
                toPurposeResponse("singForm", "노래방", selected, usages)
        );
        long purposeRemainTime = 0;
        for (PurposeUsage usage : usages) {
            if (!usage.isCompleted() && usage.getEndAt().isAfter(now)) {
                long seconds = Duration.between(now, usage.getEndAt()).getSeconds();
                purposeRemainTime = Math.max(0, (seconds + 59) / 60);
                break;
            }
        }
        return new LogDetailResponse(log.getName(), purposes, log.getOrganRemainTime(now), purposeRemainTime);
    }

    private LogDetailResponse.PurposeResponse toPurposeResponse(String form, String name, List<VisitPurpose> selected, List<PurposeUsage> usages) {
        boolean purposeSelect = selected.stream().anyMatch(purpose -> name.equals(purpose.getPurpose().getPurposeName()));
        String state = "none";
        for (PurposeUsage usage : usages) {
            if (name.equals(usage.getPurpose().getPurposeName())) {
                state = usage.isCompleted() ? "success" : "pending";
                break;
            }
        }
        return new LogDetailResponse.PurposeResponse(form, purposeSelect, state);
    }
}
