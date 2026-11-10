package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.PcFormRequest;
import com.example.burserker.beopdong.domain.check_in.exception.InvalidFormPurposeException;
import com.example.burserker.beopdong.domain.check_in.service.util.UsageLockService;
import com.example.burserker.beopdong.domain.check_in.service.util.UsageValidationService;
import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.PurposeUsage;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeUsageRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.VisitPurposeRepository;
import com.example.burserker.beopdong.domain.log.facade.LogFacade;
import com.example.burserker.beopdong.domain.log.service.CompletePurposeUsageService;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CreatePcFormService {
    private final PurposeUsageRepository purposeUsageRepository;
    private final VisitPurposeRepository visitPurposeRepository;
    private final LogFacade logFacade;
    private final UsageValidationService usageValidationService;
    private final UsageLockService usageLockService;
    private final CompletePurposeUsageService completePurposeUsageService;

    /**
     * 공통 잠금 안에서 방문의 선택 목록, 회원 이용 상태와 좌석을 검사하고 컴퓨터 이용을 시작한다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void execute(PcFormRequest request) {
        usageLockService.lock();

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        completePurposeUsageService.execute(now);

        Log log = logFacade.getLogById(request.logId());
        Purpose purpose = visitPurposeRepository.findByLogIdAndPurposePurposeName(log.getId(), "인터넷")
                .orElseThrow(() -> InvalidFormPurposeException.EXCEPTION).getPurpose();

        usageValidationService.validateUser(log.getUser().getId(), now);

        usageValidationService.validatePcSeat(purpose.getId(), request.seat(), now);
        purposeUsageRepository.save(PurposeUsage.createPc(purpose, log, request.seat(), request.usePurpose(), now));
    }
}
