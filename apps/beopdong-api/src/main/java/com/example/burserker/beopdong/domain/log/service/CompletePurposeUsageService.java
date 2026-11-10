package com.example.burserker.beopdong.domain.log.service;

import com.example.burserker.beopdong.domain.check_in.service.util.UsageLockService;
import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.log.domain.PurposeUsage;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeLogRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CompletePurposeUsageService {

    private final PurposeUsageRepository purposeUsageRepository;
    private final PurposeLogRepository purposeLogRepository;
    private final UsageLockService usageLockService;

    /**
     * 요청 처리 중 만료된 회차를 정리한다. 공통 잠금과 완료 표시로 회차마다 이력을 한 번만 생성한다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void execute() {
        execute(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    }

    /**
     * 요청 처리 중 만료된 회차를 정리한다. 공통 잠금과 완료 표시로 회차마다 이력을 한 번만 생성한다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void execute(LocalDateTime now) {
        usageLockService.lock();
        for (PurposeUsage usage : purposeUsageRepository.findAllByCompletedFalseAndEndAtLessThanEqual(now)) {
            purposeLogRepository.save(PurposeLog.complete(usage));
            usage.complete();
        }
    }
}
