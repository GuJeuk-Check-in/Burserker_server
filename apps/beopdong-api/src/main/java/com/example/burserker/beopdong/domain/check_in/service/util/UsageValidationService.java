package com.example.burserker.beopdong.domain.check_in.service.util;

import com.example.burserker.beopdong.domain.check_in.exception.UsageAlreadyActiveException;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsageValidationService {

    private final PurposeUsageRepository purposeUsageRepository;

    public void validateUser(Long userId, LocalDateTime now) {
        if (purposeUsageRepository.existsActiveUser(userId, now)) {
            throw UsageAlreadyActiveException.EXCEPTION;
        }
    }

    public void validatePcSeat(Long purposeId, int seat, LocalDateTime now) {
        if (purposeUsageRepository.existsByPurposeIdAndSeatAndCompletedFalseAndEndAtAfter(purposeId, seat, now)) {
            throw UsageAlreadyActiveException.EXCEPTION;
        }
    }
}
