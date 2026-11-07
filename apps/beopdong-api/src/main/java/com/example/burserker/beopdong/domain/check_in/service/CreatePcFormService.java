package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.PcFormRequest;
import com.example.burserker.beopdong.domain.check_in.service.util.UsageLockService;
import com.example.burserker.beopdong.domain.check_in.service.util.UsageValidationService;
import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeLogRepository;
import com.example.burserker.beopdong.domain.log.facade.LogFacade;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.check_in.exception.InvalidFormPurposeException;
import com.example.burserker.beopdong.domain.check_in.exception.DailyUsageExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CreatePcFormService {

    private final PurposeLogRepository purposeLogRepository;
    private final LogFacade logFacade;
    private final UsageValidationService usageValidationService;
    private final UsageLockService usageLockService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void execute(PcFormRequest request) {
        usageLockService.lock();

        Log log = logFacade.getLogById(request.logId());

        PurposeLog purposeLog = purposeLogRepository
                .findByLogIdAndPurposePurposeName(log.getId(), "인터넷")
                .orElseThrow(() -> InvalidFormPurposeException.EXCEPTION);

        Purpose purpose = purposeLog.getPurpose();

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        usageValidationService.validateUser(log.getUser().getId(), purpose.getId(), now);

        usageValidationService.validatePcSeat(purpose.getId(), request.seat(), now);

        if (purposeLog.getPurposeState() != PurposeState.NONE
                || purposeLog.getStartAt() != null) {
            throw DailyUsageExceededException.EXCEPTION;
        }

        purposeLog.startPc(request.seat(), request.usePurpose(), now, 40);
    }
}