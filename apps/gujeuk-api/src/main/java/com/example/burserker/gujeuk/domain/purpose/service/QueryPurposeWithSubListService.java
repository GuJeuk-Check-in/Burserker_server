package com.example.burserker.gujeuk.domain.purpose.service;

import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.SubPurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.PurposeWithSubListResponse;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.PurposeWithSubResponse;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.SubPurposeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueryPurposeWithSubListService {
    private final PurposeRepository purposeRepository;
    private final SubPurposeRepository subPurposeRepository;

    @Transactional(readOnly = true)
    public PurposeWithSubListResponse execute() {
        // 하위 방문목적을 한 번에 조회해 상위 방문목적 ID별로 묶는다 (정렬 순서 유지)
        Map<Long, List<SubPurposeResponse>> subPurposesByPurposeId =
                subPurposeRepository.findAllOrderByPurposeIdAndSubPurposeIndex().stream()
                        .collect(Collectors.groupingBy(
                                subPurpose -> subPurpose.getPurpose().getId(),
                                Collectors.mapping(SubPurposeResponse::from, Collectors.toList())));

        List<PurposeWithSubResponse> purposes = purposeRepository.findAllByOrderByPurposeIndexAscIdAsc().stream()
                .map(purpose -> PurposeWithSubResponse.of(
                        purpose, subPurposesByPurposeId.getOrDefault(purpose.getId(), List.of())))
                .toList();

        return new PurposeWithSubListResponse(purposes);
    }
}
