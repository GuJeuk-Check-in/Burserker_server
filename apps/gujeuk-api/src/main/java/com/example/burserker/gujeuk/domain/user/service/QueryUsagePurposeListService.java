package com.example.burserker.gujeuk.domain.user.service;

import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.SubPurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.SubPurposeResponse;
import com.example.burserker.gujeuk.domain.user.presentation.dto.response.UsagePurposeListResponse;
import com.example.burserker.gujeuk.domain.user.presentation.dto.response.UsagePurposeResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryUsagePurposeListService {
    private final PurposeRepository purposeRepository;
    private final SubPurposeRepository subPurposeRepository;

    @Transactional(readOnly = true)
    public UsagePurposeListResponse execute() {
        // 하위 방문목적을 한 번에 조회해 상위 방문목적 ID별로 묶는다 (정렬 순서 유지)
        Map<Long, List<SubPurposeResponse>> subPurposesByPurposeId =
                subPurposeRepository.findAllOrderByPurposeIdAndSubPurposeIndex().stream()
                        .collect(Collectors.groupingBy(
                                subPurpose -> subPurpose.getPurpose().getId(),
                                Collectors.mapping(SubPurposeResponse::from, Collectors.toList())));

        return new UsagePurposeListResponse(
                purposeRepository.findAllByOrderByPurposeIndexAsc().stream()
                        .map(purpose -> UsagePurposeResponse.of(
                                purpose, subPurposesByPurposeId.getOrDefault(purpose.getId(), List.of())))
                        .toList()
        );
    }
}
