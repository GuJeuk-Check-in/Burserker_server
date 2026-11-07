package com.example.burserker.beopdong.domain.log.facade;

import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogFacade {

    private final LogRepository logRepository;

    public Log getLogById(Long logId) {
        return logRepository.findById(logId)
                .orElseThrow(() -> new BeopdongException(ErrorCode.LOG_NOT_FOUND));
    }
}