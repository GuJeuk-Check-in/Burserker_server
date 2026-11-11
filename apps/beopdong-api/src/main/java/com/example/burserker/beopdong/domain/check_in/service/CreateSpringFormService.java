package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.SpringFormRequest;
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
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CreateSpringFormService {
    private final PurposeUsageRepository purposeUsageRepository;
    private final VisitPurposeRepository visitPurposeRepository;
    private final LogFacade logFacade;
    private final UsageValidationService usageValidationService;
    private final UsageLockService usageLockService;
    private final CompletePurposeUsageService completePurposeUsageService;
    private final PurposeRepository purposeRepository;

    /**
     * 노래방·탁구를 한 명씩 신청한다. 빈 시설은 새 회차를 만들고, 사용 중이면 정원 안에서 합류한다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void execute(SpringFormRequest request) {
        usageLockService.lock();

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        completePurposeUsageService.execute(now);

        Log log = logFacade.getLogById(request.logId());
        Purpose purpose = purposeRepository.findById(request.purpose())
                .orElseThrow(() -> InvalidFormPurposeException.EXCEPTION);

        if ((!"노래방".equals(purpose.getPurposeName()) && !"탁구".equals(purpose.getPurposeName()))
                || !visitPurposeRepository.existsByLogIdAndPurposeId(log.getId(), purpose.getId())) {
            throw InvalidFormPurposeException.EXCEPTION;
        }

        usageValidationService.validateUser(log.getUser().getId(), now);

        PurposeUsage usage = purposeUsageRepository
                .findFirstByPurposeIdAndCompletedFalseAndEndAtAfterOrderByIdAsc(purpose.getId(), now)
                .orElse(null);

        if (usage == null) {
            usage = PurposeUsage.createRoom(purpose, log, now);
        } else {
            usage.join(log, now);
        }
        purposeUsageRepository.save(usage);
    }
}
