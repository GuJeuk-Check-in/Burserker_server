package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.SpringFormRequest;
import com.example.burserker.beopdong.domain.check_in.service.util.UsageLockService;
import com.example.burserker.beopdong.domain.check_in.service.util.UsageValidationService;
import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeLogRepository;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.log.facade.LogFacade;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.check_in.exception.InvalidFormPurposeException;
import com.example.burserker.beopdong.domain.check_in.exception.DailyUsageExceededException;
import com.example.burserker.beopdong.domain.check_in.exception.InvalidUsageCountException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CreateSpringFormService {
    private final LogFacade logFacade;
    private final PurposeLogRepository purposeLogRepository;
    private final UsageValidationService usageValidationService;
    private final PurposeRepository purposeRepository;
    private final UsageLockService usageLockService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void execute(SpringFormRequest request) {
        usageLockService.lock();

        Purpose purpose = purposeRepository.findById(request.purpose())
                .orElseThrow(() -> InvalidFormPurposeException.EXCEPTION);

        int usageMinutes;

        if ("노래방".equals(purpose.getPurposeName())) {
            if (request.count() > 2) {
                throw InvalidUsageCountException.EXCEPTION;
            }
            usageMinutes = request.count() == 1 ? 20 : 30;
        } else if ("탁구".equals(purpose.getPurposeName())) {
            usageMinutes = 40;
        } else {
            throw InvalidFormPurposeException.EXCEPTION;
        }

        Log log = logFacade.getLogById(request.logId());

        PurposeLog purposeLog = purposeLogRepository
                .findByLogIdAndPurposeId(log.getId(), purpose.getId())
                .orElseThrow(() -> InvalidFormPurposeException.EXCEPTION);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        usageValidationService.validateUser(log.getUser().getId(), purpose.getId(), now);

        usageValidationService.validateSping(purpose.getId(), now);

        if (purposeLog.getPurposeState() != PurposeState.NONE
                || purposeLog.getStartAt() != null) {
            throw DailyUsageExceededException.EXCEPTION;
        }

        purposeLog.startSping(request.count(), now, usageMinutes);
    }
}