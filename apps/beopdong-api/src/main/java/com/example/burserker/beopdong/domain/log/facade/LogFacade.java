package com.example.burserker.beopdong.domain.log.facade;

import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.log.exception.LogNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogFacade {

    private final LogRepository logRepository;

    public Log getLogById(Long logId) {
        return logRepository.findById(logId)
                .orElseThrow(() -> LogNotFoundException.EXCEPTION);
    }
}