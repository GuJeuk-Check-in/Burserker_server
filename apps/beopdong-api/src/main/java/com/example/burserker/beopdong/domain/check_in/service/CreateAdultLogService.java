package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.check_in.exception.CheckInPurposeNotFoundException;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.CreateAdultLogRequest;
import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.VisitPurpose;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.VisitPurposeRepository;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateAdultLogService {
    private final LogRepository logRepository;
    private final PurposeRepository purposeRepository;
    private final VisitPurposeRepository visitPurposeRepository;

    @Transactional
    public void execute(CreateAdultLogRequest request) {
        List<Long> ids = request.purpose().stream().distinct().toList();
        List<Purpose> purposes = purposeRepository.findAllById(ids);
        if (purposes.size() != ids.size()) {
            throw CheckInPurposeNotFoundException.EXCEPTION;
        }
        Log log = logRepository.save(Log.createAdult(request.gender(), LocalDateTime.now(ZoneId.of("Asia/Seoul"))));
        for (Purpose purpose : purposes) {
            visitPurposeRepository.save(VisitPurpose.create(log, purpose));
        }
    }
}
