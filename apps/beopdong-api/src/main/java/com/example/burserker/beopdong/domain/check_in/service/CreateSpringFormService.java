package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.SpringFormRequest;
import com.example.burserker.beopdong.domain.check_in.service.util.UsageValidationService;
import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.PurposeLog;
import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeLogRepository;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.log.facade.LogFacade;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    @Transactional
    public void execute(SpringFormRequest request) {
        Purpose purpose = purposeRepository.findById(request.purpose())
                .orElseThrow(() -> new BeopdongException(ErrorCode.INVALID_FORM_PURPOSE));

        int usageMinutes;

        switch (purpose.getPurposeName()) {
            case "노래방" -> {
                if (request.count() > 2) {
                    throw new BeopdongException(ErrorCode.BAD_REQUEST, "이용 인원을 다시 확인해주세요.");
                }
                usageMinutes = request.count() == 1 ? 20 : 30;
            }

            case "탁구" -> usageMinutes = 40;

            default -> throw new BeopdongException(ErrorCode.INVALID_FORM_PURPOSE);
        }

        Log log = logFacade.getLogById(request.logId());

        PurposeLog purposeLog = purposeLogRepository
                .findByLogIdAndPurposeId(log.getId(), purpose.getId())
                .orElseThrow(() -> new BeopdongException(ErrorCode.INVALID_FORM_PURPOSE));

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        usageValidationService.validateUser(log.getUser().getId(), purpose.getId(), now);

        usageValidationService.validateSping(purpose.getId(), now);

        if (purposeLog.getPurposeState() != PurposeState.NONE
                || purposeLog.getStartAt() != null) {
            throw new BeopdongException(ErrorCode.DAILY_USAGE_EXCEEDED);
        }

        purposeLog.startSping(request.count(), now, usageMinutes);
    }
}