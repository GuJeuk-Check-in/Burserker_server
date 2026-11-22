package com.example.burserker.beopdong.domain.log.service;

import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.VisitPurpose;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.VisitPurposeRepository;
import com.example.burserker.beopdong.domain.log.exception.OrganLogPurposeNotFoundException;
import com.example.burserker.beopdong.domain.log.presentation.dto.request.OrganLogRequest;
import com.example.burserker.beopdong.domain.log.presentation.dto.response.CreateOrganLogResponse;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.log.exception.OrganLogRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateOrganLogService {
    private final LogRepository logRepository;
    private final VisitPurposeRepository visitPurposeRepository;
    private final PurposeRepository purposeRepository;

    @Transactional
    public CreateOrganLogResponse execute(OrganLogRequest request) {
        if (request.age() == null || request.gender() == null) {
            throw OrganLogRequiredException.EXCEPTION;
        }
        if (request.age() < 20 && (request.name() == null || request.name().isBlank()
                || request.phone() == null || request.phone().isBlank())) {
            throw OrganLogRequiredException.EXCEPTION;
        }
        List<Long> ids = request.purpose().stream().distinct().toList();
        List<Purpose> purposes = purposeRepository.findAllById(ids);
        if (purposes.size() != ids.size()) {
            throw OrganLogPurposeNotFoundException.EXCEPTION;
        }
        Log log = logRepository.save(Log.createForOrgan(request.name(), request.age(), request.gender(), request.phone(),
                request.visitDate().atTime(request.visitTime()), request.visitPurpose(), request.note()));
        for (Purpose purpose : purposes) {
            visitPurposeRepository.save(VisitPurpose.create(log, purpose));
        }
        return new CreateOrganLogResponse(log.getId());
    }
}
