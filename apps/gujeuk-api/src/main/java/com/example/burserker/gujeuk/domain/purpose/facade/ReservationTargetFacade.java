package com.example.burserker.gujeuk.domain.purpose.facade;

import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.ReservationTarget;
import com.example.burserker.gujeuk.domain.purpose.domain.SubPurpose;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationStatus;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.domain.repository.SubPurposeRepository;
import com.example.burserker.gujeuk.domain.purpose.exception.PurposeNotFoundException;
import com.example.burserker.gujeuk.domain.purpose.exception.ReservationNotAllowedException;
import com.example.burserker.gujeuk.domain.purpose.exception.ReservationTargetRequiredException;
import com.example.burserker.gujeuk.domain.purpose.exception.SubPurposeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationTargetFacade {
    private final PurposeRepository purposeRepository;
    private final SubPurposeRepository subPurposeRepository;

    /**
     * 예약할 단위를 찾고 예약 가능한지 검사한다.
     * subPurposeId가 있으면 하위 방문목적, 없으면 purposeId 또는 방문목적 이름(기존 앱 호환)으로 상위 방문목적을 찾는다.
     * 하위 방문목적은 상위가 다르면 이름이 겹칠 수 있어 ID로만 받는다.
     */
    public ReservationTarget getReservableTarget(Long organId, String purposeName, Long purposeId, Long subPurposeId) {
        ReservationTarget target = subPurposeId != null
                ? ReservationTarget.of(getSubPurpose(organId, purposeId, subPurposeId))
                : ReservationTarget.of(getPurpose(organId, purposeName, purposeId));

        if (!isReservable(target)) {
            throw ReservationNotAllowedException.EXCEPTION;
        }

        return target;
    }

    /**
     * 기관의 예약 가능한 단위를 상위 순서(purposeIndex·id), 하위 순서(subPurposeIndex)대로 평탄하게 돌려준다.
     */
    public List<ReservationTarget> getReservableTargets(Long organId) {
        Map<Long, List<SubPurpose>> subPurposesByPurposeId = subPurposeRepository.findAllByOrganIdOrderByIndex(organId)
                .stream()
                .collect(Collectors.groupingBy(s -> s.getPurpose().getId(), Collectors.toList()));

        List<ReservationTarget> targets = new ArrayList<>();
        for (Purpose purpose : purposeRepository.findAllByOrganIdOrderByPurposeIndexAscIdAsc(organId)) {
            if (purpose.getReservationStatus() == ReservationStatus.RESERVATION) {
                targets.add(ReservationTarget.of(purpose));
            } else if (purpose.getReservationStatus() == ReservationStatus.SUB_INCLUDE) {
                subPurposesByPurposeId.getOrDefault(purpose.getId(), List.of()).stream()
                        .filter(SubPurpose::isReservable)
                        .map(subPurpose -> new ReservationTarget(purpose, subPurpose))
                        .forEach(targets::add);
            }
        }
        return targets;
    }

    public static boolean isReservable(ReservationTarget target) {
        if (target.subPurpose() == null) {
            return target.purpose().getReservationStatus() == ReservationStatus.RESERVATION;
        }
        return target.purpose().getReservationStatus() == ReservationStatus.SUB_INCLUDE
                && target.subPurpose().isReservable();
    }

    private SubPurpose getSubPurpose(Long organId, Long purposeId, Long subPurposeId) {
        SubPurpose subPurpose = subPurposeRepository.findWithPurposeByIdAndOrganId(subPurposeId, organId)
                .orElseThrow(() -> SubPurposeNotFoundException.EXCEPTION);

        if (purposeId != null && !purposeId.equals(subPurpose.getPurpose().getId())) {
            throw SubPurposeNotFoundException.EXCEPTION;
        }

        return subPurpose;
    }

    private Purpose getPurpose(Long organId, String purposeName, Long purposeId) {
        if (purposeId != null) {
            return purposeRepository.findByIdAndOrganId(purposeId, organId)
                    .orElseThrow(() -> PurposeNotFoundException.EXCEPTION);
        }

        if (purposeName == null || purposeName.isBlank()) {
            throw ReservationTargetRequiredException.EXCEPTION;
        }

        return purposeRepository.findByOrganIdAndPurposeName(organId, purposeName.trim())
                .orElseThrow(() -> PurposeNotFoundException.EXCEPTION);
    }
}
