package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.check_in.exception.InvalidVisitPurposeException;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.UpdateVisitPurposeRequest;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.response.UpdateVisitPurposeResponse;
import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.exception.LogCheckedOutException;
import com.example.burserker.beopdong.domain.log.domain.VisitPurpose;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.VisitPurposeRepository;
import com.example.burserker.beopdong.domain.log.exception.LogDetailNotFoundException;
import com.example.burserker.beopdong.domain.log.service.QueryLogDetailService;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.purpose.domain.PurposeUsage;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateVisitPurposeService {
    private final LogRepository logRepository;
    private final VisitPurposeRepository visitPurposeRepository;
    private final PurposeRepository purposeRepository;
    private final PurposeUsageRepository purposeUsageRepository;
    private final QueryLogDetailService queryLogDetailService;

    @Transactional
    public UpdateVisitPurposeResponse execute(UpdateVisitPurposeRequest request) {
        Log log = logRepository.findById(request.logId())
                .orElseThrow(() -> LogDetailNotFoundException.EXCEPTION);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        if (log.isCheckedOut()) {
            throw LogCheckedOutException.EXCEPTION;
        }
        List<Long> purposeIds = request.purpose().stream().distinct().toList();
        List<Purpose> purposes = new ArrayList<>(purposeRepository.findAllById(purposeIds));
        if (purposes.size() != purposeIds.size()) {
            throw InvalidVisitPurposeException.EXCEPTION;
        }
        Purpose rest = purposeRepository.findFirstByPurposeNameOrderByIdAsc("휴식")
                .orElseThrow(() -> InvalidVisitPurposeException.EXCEPTION);
        if (purposes.stream().noneMatch(purpose -> purpose.getId().equals(rest.getId()))) {
            purposes.add(rest);
        }
        for (PurposeUsage usage : purposeUsageRepository.findUsagesByLogId(log.getId())) {
            if (!usage.isCompleted() && usage.getEndAt().isAfter(now)
                    && !purposeIds.contains(usage.getPurpose().getId())) {
                throw InvalidVisitPurposeException.EXCEPTION;
            }
        }
        List<VisitPurpose> previous = visitPurposeRepository.findAllByLogId(log.getId());
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
        return new UpdateVisitPurposeResponse(queryLogDetailService.execute(log.getId()).purpose());
    }
}
