package com.example.burserker.beopdong.domain.log.service;

import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.log.exception.LogNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteOrganLogService {
    private final LogRepository logRepository;

    @Transactional
    public void execute(Long logId) {
        logRepository.findById(logId).orElseThrow(() -> LogNotFoundException.EXCEPTION).markDeleted();
    }
}
