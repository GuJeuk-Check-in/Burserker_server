package com.example.burserker.beopdong.domain.log.service;

import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.VisitPurpose;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.VisitPurposeRepository;
import com.example.burserker.beopdong.domain.log.exception.LogNotFoundException;
import com.example.burserker.beopdong.domain.log.exception.OrganLogPurposeNotFoundException;
import com.example.burserker.beopdong.domain.log.presentation.dto.request.OrganLogRequest;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import com.example.burserker.beopdong.domain.purpose.domain.PurposeUsage;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeUsageRepository;
import com.example.burserker.beopdong.domain.check_in.exception.InvalidVisitPurposeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateOrganLogService {
    private final LogRepository logRepository;
    private final VisitPurposeRepository visitPurposeRepository;
    private final PurposeRepository purposeRepository;
    private final PurposeUsageRepository purposeUsageRepository;

    @Transactional
    public void execute(Long logId, OrganLogRequest request) {
        Log log = logRepository.findById(logId).orElseThrow(() -> LogNotFoundException.EXCEPTION);
        if (request.age() < 20 && (request.phone() == null || request.phone().isBlank())) {
            throw new BeopdongException(ErrorCode.ORGAN_LOG_REQUIRED, "연락처는 필수 입력값입니다.");
        }
        List<Long> ids = request.purpose().stream().distinct().toList();
        List<Purpose> purposes = purposeRepository.findAllById(ids);
        if (purposes.size() != ids.size()) {
            throw OrganLogPurposeNotFoundException.EXCEPTION;
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        for (PurposeUsage usage : purposeUsageRepository.findUsagesByLogId(logId)) {
            if (!usage.isCompleted() && usage.getEndAt().isAfter(now)
                    && !ids.contains(usage.getPurpose().getId())) {
                throw InvalidVisitPurposeException.EXCEPTION;
            }
        }
        log.update(request.name(), request.age(), request.gender(), request.phone(),
                request.visitDate().atTime(request.visitTime()), request.visitPurpose(), request.note());
        List<VisitPurpose> previous = visitPurposeRepository.findAllByLogId(logId);
        for (VisitPurpose selected : previous) {
            if (purposes.stream().noneMatch(purpose -> purpose.getId().equals(selected.getPurpose().getId()))) {
                visitPurposeRepository.delete(selected);
            }
        }
        for (Purpose purpose : purposes) {
            if (previous.stream().noneMatch(selected -> selected.getPurpose().getId().equals(purpose.getId()))) {
                visitPurposeRepository.save(VisitPurpose.create(log, purpose));
            }
        }
    }
}
