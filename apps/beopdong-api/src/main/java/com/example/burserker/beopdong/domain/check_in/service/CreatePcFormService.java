package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.PcFormRequest;
import com.example.burserker.beopdong.domain.check_in.exception.InvalidFormPurposeException;
import com.example.burserker.beopdong.domain.check_in.exception.UsageAlreadyActiveException;
import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.exception.LogCheckedOutException;
import com.example.burserker.beopdong.domain.purpose.domain.PurposeUsage;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeUsageRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.VisitPurposeRepository;
import com.example.burserker.beopdong.domain.log.facade.LogFacade;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CreatePcFormService {
    private final PurposeUsageRepository purposeUsageRepository;
    private final VisitPurposeRepository visitPurposeRepository;
    private final LogFacade logFacade;

    /**
     * 방문의 선택 목록, 회원 이용 상태와 좌석을 검사하고 컴퓨터 이용을 시작한다.
     */
    @Transactional
    public void execute(PcFormRequest request) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        Log log = logFacade.getLogById(request.logId());
        if (log.isCheckedOut()) {
            throw LogCheckedOutException.EXCEPTION;
        }
        if (log.getAge() == null || log.getAge() >= 20) {
            throw InvalidFormPurposeException.EXCEPTION;
        }
        Purpose purpose = visitPurposeRepository.findByLogIdAndPurposePurposeName(log.getId(), "인터넷")
                .orElseThrow(() -> InvalidFormPurposeException.EXCEPTION).getPurpose();

        boolean active = log.getUser() == null
                ? purposeUsageRepository.existsActiveLog(log.getId(), now)
                : purposeUsageRepository.existsActiveUser(log.getUser().getId(), now);
        if (active) {
            throw UsageAlreadyActiveException.EXCEPTION;
        }

        if (purposeUsageRepository.existsActiveSeat(purpose.getId(), request.seat(), now)) {
            throw UsageAlreadyActiveException.EXCEPTION;
        }
        purposeUsageRepository.save(PurposeUsage.createPc(purpose, log, request.seat(), request.usePurpose(), now));
    }
}
