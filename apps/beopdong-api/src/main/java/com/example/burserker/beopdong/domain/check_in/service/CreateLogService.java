package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.check_in.exception.CheckInPurposeNotFoundException;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.CreateLogRequest;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.response.UserSignUpResponse;
import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.VisitPurpose;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.VisitPurposeRepository;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.user.domain.User;
import com.example.burserker.beopdong.domain.user.domain.repository.UserRepository;
import com.example.burserker.beopdong.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateLogService {
    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final PurposeRepository purposeRepository;
    private final VisitPurposeRepository visitPurposeRepository;

    @Transactional
    public UserSignUpResponse execute(CreateLogRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
        List<Long> purposeIds = request.purpose().stream().distinct().toList();
        List<Purpose> purposes = new ArrayList<>(purposeRepository.findAllById(purposeIds));
        if (purposes.size() != purposeIds.size()) {
            throw CheckInPurposeNotFoundException.EXCEPTION;
        }
        Purpose rest = purposeRepository.findFirstByPurposeNameOrderByIdAsc("휴식")
                .orElseThrow(() -> CheckInPurposeNotFoundException.EXCEPTION);
        if (purposes.stream().noneMatch(purpose -> purpose.getId().equals(rest.getId()))) {
            purposes.add(rest);
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        Log log = logRepository.save(Log.create(user, now));
        for (Purpose purpose : purposes) {
            visitPurposeRepository.save(VisitPurpose.create(log, purpose));
        }
        user.increaseVisitCount();
        return UserSignUpResponse.from(log.getId(), purposes);
    }
}
