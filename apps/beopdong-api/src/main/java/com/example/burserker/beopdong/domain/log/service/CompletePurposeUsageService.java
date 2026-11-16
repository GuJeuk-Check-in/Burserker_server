package com.example.burserker.beopdong.domain.log.service;

import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.purpose.domain.PurposeUsage;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeLogRepository;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompletePurposeUsageService {

    private final PurposeUsageRepository purposeUsageRepository;
    private final PurposeLogRepository purposeLogRepository;

    /**
     * 종료 시각이 지난 미완료 이용을 찾아 완료 이력을 생성한다.
     */
    @Transactional
    public void execute(LocalDateTime now) {
        for (PurposeUsage usage : purposeUsageRepository.findExpiredUsages(now)) {
            purposeLogRepository.save(PurposeLog.complete(usage));
            usage.complete();
        }
    }
}
