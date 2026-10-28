package com.example.burserker.beopdong.domain.purpose.service;

import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.purpose.exception.PurposeNotFoundException;
import com.example.burserker.beopdong.domain.purpose.presentation.dto.request.PurposeMoveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovePurposeService {
    private final PurposeRepository purposeRepository;

    @Transactional
    public void execute(PurposeMoveRequest purposeMoveRequest) {
        List<Long> purposeId = purposeMoveRequest.purposeId();
        List<Purpose> purposes = purposeRepository.findAllById(purposeMoveRequest.purposeId());

        if(purposeId.size() != purposes.size()) {
            throw PurposeNotFoundException.EXCEPTION;
        }

        Map<Long, Purpose> purposeMap = purposes.stream()
                .collect(Collectors.toMap(Purpose::getId, purpose -> purpose));

        for(int i = 0; i < purposeMoveRequest.purposeId().size(); i++) {
            Purpose purpose = purposeMap.get(purposeMoveRequest.purposeId().get(i));

            purpose.updatePurposeIndex(i + 1);
        }
    }
}
