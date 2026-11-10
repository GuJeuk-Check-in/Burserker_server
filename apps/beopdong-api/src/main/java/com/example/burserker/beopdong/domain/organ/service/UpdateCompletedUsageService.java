package com.example.burserker.beopdong.domain.organ.service;

import com.example.burserker.beopdong.domain.check_in.service.util.UsageLockService;
import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeLogRepository;
import com.example.burserker.beopdong.domain.organ.exception.InvalidCompletedUsageException;
import com.example.burserker.beopdong.domain.organ.presentation.dto.request.UpdateCompletedUsageRequest;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.purpose.exception.PurposeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class UpdateCompletedUsageService {

    private final PurposeLogRepository purposeLogRepository;
    private final PurposeRepository purposeRepository;
    private final UsageLockService usageLockService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void execute(Long id, UpdateCompletedUsageRequest request) {
        usageLockService.lock();

        PurposeLog usage = purposeLogRepository.findById(id)
                .orElseThrow(() -> PurposeNotFoundException.EXCEPTION);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        if (usage.getPurposeState() != PurposeState.SUCCESS) {
            throw InvalidCompletedUsageException.EXCEPTION;
        }

        Purpose purpose = purposeRepository.findById(request.purpose())
                .orElseThrow(() -> PurposeNotFoundException.EXCEPTION);

        LocalDateTime startAt = request.visitDate().atTime(request.startTime());
        LocalDateTime endAt = request.visitDate().atTime(request.endTime());

        if (endAt.isBefore(startAt) || endAt.isAfter(now)) {
            throw InvalidCompletedUsageException.EXCEPTION;
        }

        Integer seat = null;
        Integer count = null;
        String usePurpose = null;

        switch (purpose.getPurposeName()) {
            case "인터넷" -> {
                if (request.seat() == null || request.seat() < 1 || request.seat() > 10
                        || request.usePurpose() == null || request.usePurpose().isBlank()) {
                    throw InvalidCompletedUsageException.EXCEPTION;
                }
                seat = request.seat();
                usePurpose = request.usePurpose();
            }
            case "탁구", "노래방" -> {
                int maxCount = "노래방".equals(purpose.getPurposeName()) ? 2 : 4;
                if (request.count() == null || request.count() < 1 || request.count() > maxCount) {
                    throw InvalidCompletedUsageException.EXCEPTION;
                }
                count = request.count();
            }
            default -> throw InvalidCompletedUsageException.EXCEPTION;
        }

        usage.updateCompletedUsage(purpose, request.name(), usePurpose, seat, count, startAt, endAt);
    }
}
