package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.SpringFormRequest;
import com.example.burserker.beopdong.domain.check_in.exception.InvalidFormPurposeException;
import com.example.burserker.beopdong.domain.check_in.exception.UsageAlreadyActiveException;
import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.purpose.domain.PurposeUsage;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeUsageRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.VisitPurposeRepository;
import com.example.burserker.beopdong.domain.log.facade.LogFacade;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CreateSpringFormService {
    private final PurposeUsageRepository purposeUsageRepository;
    private final VisitPurposeRepository visitPurposeRepository;
    private final LogFacade logFacade;
    private final PurposeRepository purposeRepository;

    /**
     * 노래방·탁구를 한 명씩 신청한다. 빈 시설은 새 회차를 만들고, 사용 중이면 정원 안에서 합류한다.
     */
    @Transactional
    public void execute(SpringFormRequest request) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        Log log = logFacade.getLogById(request.logId());
        Purpose purpose = purposeRepository.findById(request.purpose())
                .orElseThrow(() -> InvalidFormPurposeException.EXCEPTION);

        if ((!"노래방".equals(purpose.getPurposeName()) && !"탁구".equals(purpose.getPurposeName()))
                || !visitPurposeRepository.existsByLogIdAndPurposeId(log.getId(), purpose.getId())) {
            throw InvalidFormPurposeException.EXCEPTION;
        }

        boolean active = log.getUser() == null
                ? purposeUsageRepository.existsActiveLog(log.getId(), now)
                : purposeUsageRepository.existsActiveUser(log.getUser().getId(), now);
        if (active) {
            throw UsageAlreadyActiveException.EXCEPTION;
        }

        PurposeUsage usage = purposeUsageRepository
                .findActiveRoomUsage(purpose.getId(), now)
                .orElse(null);

        if (usage == null) {
            usage = PurposeUsage.createRoom(purpose, log, now);
        } else {
            usage.join(log, now);
        }
        purposeUsageRepository.save(usage);
    }
}
