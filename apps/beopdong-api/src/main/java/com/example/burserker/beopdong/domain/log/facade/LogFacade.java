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

    /**
     * 방명록을 조회하고 없으면 전용 예외를 발생시킨다. 요청자의 방문 소유권을 인증하는 처리는 아니다.
     */
    public Log getLogById(Long logId) {
        return logRepository.findById(logId)
                .orElseThrow(() -> LogNotFoundException.EXCEPTION);
    }
}
