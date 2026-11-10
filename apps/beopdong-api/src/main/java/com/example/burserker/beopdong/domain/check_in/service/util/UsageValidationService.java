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

    /**
     * 참가자 전체를 기준으로 해당 회원의 동시 시설 이용을 막는다. 종료된 이용의 당일 재신청은 허용한다.
     */
    public void validateUser(Long userId, LocalDateTime now) {
        if (purposeUsageRepository.existsActiveUser(userId, now)) {
            throw UsageAlreadyActiveException.EXCEPTION;
        }
    }

    /**
     * 종료 시각이 지나지 않은 회차가 같은 컴퓨터 좌석을 점유하면 신청을 거절한다.
     */
    public void validatePcSeat(Long purposeId, int seat, LocalDateTime now) {
        if (purposeUsageRepository.existsByPurposeIdAndSeatAndCompletedFalseAndEndAtAfter(purposeId, seat, now)) {
            throw UsageAlreadyActiveException.EXCEPTION;
        }
    }
}
