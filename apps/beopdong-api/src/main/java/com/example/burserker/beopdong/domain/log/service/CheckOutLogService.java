package com.example.burserker.beopdong.domain.log.service;

import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.log.exception.CheckOutUsageActiveException;
import com.example.burserker.beopdong.domain.log.exception.LogNotFoundException;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CheckOutLogService {
    private final LogRepository logRepository;
    private final PurposeUsageRepository purposeUsageRepository;

    @Transactional
    public void execute(Long logId) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> LogNotFoundException.EXCEPTION);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        if (log.isCheckedOut()) {
            return;
        }
        if (purposeUsageRepository.existsActiveLog(logId, now)) {
            throw CheckOutUsageActiveException.EXCEPTION;
        }
        log.checkOut(now);
    }
}
