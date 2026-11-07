package com.example.burserker.beopdong.domain.check_in.service.util;

import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeLogRepository;
import com.example.burserker.beopdong.domain.check_in.exception.UsageAlreadyActiveException;
import com.example.burserker.beopdong.domain.check_in.exception.DailyUsageExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsageValidationService {

    private final PurposeLogRepository purposeLogRepository;

    public void validateUser(Long userId, Long purposeId, LocalDateTime now) {
        if (purposeLogRepository
                .existsByLogUserIdAndPurposeStateAndEndAtAfter(
                        userId,
                        PurposeState.PENDING,
                        now
                )) {
            throw UsageAlreadyActiveException.EXCEPTION;
        }

        LocalDateTime dayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime nextDayStart = dayStart.plusDays(1);

        if (purposeLogRepository.existsDailyUsage(
                userId,
                purposeId,
                dayStart,
                nextDayStart
        )) {
            throw DailyUsageExceededException.EXCEPTION;
        }
    }

    public void validatePcSeat(Long purposeId, int seat, LocalDateTime now) {
        if (purposeLogRepository
                .existsByPurposeIdAndSeatAndPurposeStateAndEndAtAfter(
                        purposeId,
                        seat,
                        PurposeState.PENDING,
                        now
                )) {
            throw UsageAlreadyActiveException.EXCEPTION;
        }
    }

    public void validateSping(Long purposeId, LocalDateTime now) {
        if (purposeLogRepository
                .existsByPurposeIdAndPurposeStateAndEndAtAfter(
                        purposeId,
                        PurposeState.PENDING,
                        now
                )) {
            throw UsageAlreadyActiveException.EXCEPTION;
        }
    }
}