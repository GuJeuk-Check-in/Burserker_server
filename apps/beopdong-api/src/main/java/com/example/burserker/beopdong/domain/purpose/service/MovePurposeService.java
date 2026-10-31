package com.example.burserker.beopdong.domain.purpose.service;

import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.purpose.exception.PurposeNotFoundException;
import com.example.burserker.beopdong.domain.purpose.presentation.dto.request.PurposeMoveRequest;
import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
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

        if (purposeId.size() != purposeRepository.count()) {
            throw new BeopdongException(
                    ErrorCode.BAD_REQUEST, "이용대장 전체를 입력해 주세요."
            );
        }

        Map<Long, Purpose> purposeMap = purposes.stream()
                .collect(Collectors.toMap(Purpose::getId, purpose -> purpose));

        for (int i = 0; i < purposeId.size(); i++) {
            Purpose purpose = purposeMap.get(purposeId.get(i));
            int newIndex = i + 1;

            if (purpose.isLocked() && purpose.getPurposeIndex() != newIndex) {
                throw new BeopdongException(
                        ErrorCode.BAD_REQUEST, "해당 이용대장의 순서는 변경할 수 없습니다."
                );
            }
        }

        for(int i = 0; i < purposeMoveRequest.purposeId().size(); i++) {
            Purpose purpose = purposeMap.get(purposeMoveRequest.purposeId().get(i));

            purpose.updatePurposeIndex(i + 1);
        }
    }
}
