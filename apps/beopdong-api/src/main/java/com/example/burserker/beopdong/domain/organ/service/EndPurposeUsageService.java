package com.example.burserker.beopdong.domain.organ.service;

import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import com.example.burserker.beopdong.domain.organ.exception.InvalidUsageEndException;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeLogRepository;
import com.example.burserker.beopdong.domain.organ.exception.PurposeUsageNotFoundException;
import com.example.burserker.beopdong.domain.purpose.domain.PurposeUsage;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeUsageRepository;
import lombok.RequiredArgsConstructor;
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
        PurposeLog history = purposeLogRepository.findByPurposeUsageIdAndPurposeState(usageId, PurposeState.PENDING)
                .orElseThrow(() -> InvalidUsageEndException.EXCEPTION);
        usage.endEarly(now);
        history.endEarly(now);
    }
}
