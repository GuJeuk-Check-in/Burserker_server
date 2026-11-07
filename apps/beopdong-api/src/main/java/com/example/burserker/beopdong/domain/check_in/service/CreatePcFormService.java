package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.PcFormRequest;
import com.example.burserker.beopdong.domain.check_in.service.util.UsageValidationService;
import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeLogRepository;
import com.example.burserker.beopdong.domain.log.facade.LogFacade;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CreatePcFormService {

    private final PurposeRepository purposeRepository;
    private final PurposeLogRepository purposeLogRepository;
    private final LogFacade logFacade;
    private final UsageValidationService usageValidationService;

    @Transactional
    public void execute(PcFormRequest request) {
        Purpose purpose = purposeRepository.findByPurposeName("인터넷")
                .orElseThrow(() -> new BeopdongException(ErrorCode.PURPOSE_NOT_FOUND));

        Log log = logFacade.getLogById(request.logId());

        PurposeLog purposeLog = purposeLogRepository
                .findByLogIdAndPurposeId(log.getId(), purpose.getId())
                .orElseThrow(() -> new BeopdongException(ErrorCode.INVALID_FORM_PURPOSE));

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        usageValidationService.validateUser(log.getUser().getId(), purpose.getId(), now);

        usageValidationService.validatePcSeat(purpose.getId(), request.seat(), now);

        if (purposeLog.getPurposeState() != PurposeState.NONE
                || purposeLog.getStartAt() != null) {
            throw new BeopdongException(ErrorCode.DAILY_USAGE_EXCEEDED);
        }

        purposeLog.startPc(request.seat(), request.usePurpose(), now, 40);
    }
}