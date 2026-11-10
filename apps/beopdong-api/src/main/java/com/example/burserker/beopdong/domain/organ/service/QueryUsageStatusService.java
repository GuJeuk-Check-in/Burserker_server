package com.example.burserker.beopdong.domain.organ.service;

import com.example.burserker.beopdong.domain.log.domain.PurposeUsage;
import com.example.burserker.beopdong.domain.log.domain.repository.PurposeUsageRepository;
import com.example.burserker.beopdong.domain.log.service.CompletePurposeUsageService;
import com.example.burserker.beopdong.domain.organ.presentation.dto.response.UsageStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryUsageStatusService {

    private final PurposeUsageRepository purposeUsageRepository;
    private final CompletePurposeUsageService completePurposeUsageService;

    /**
     * 만료 회차를 정리한 뒤 사용 중인 컴퓨터 좌석과 노래방·탁구의 팀 현황을 반환한다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UsageStatusResponse execute() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        completePurposeUsageService.execute(now);

        List<PurposeUsage> usages = purposeUsageRepository
                .findAllByCompletedFalseAndEndAtAfter(now);

        List<UsageStatusResponse.PcUsage> pc = new ArrayList<>();
        UsageStatusResponse.RoomUsage pingPong = null;
        UsageStatusResponse.RoomUsage sing = null;

        for (PurposeUsage usage : usages) {
            switch (usage.getPurpose().getPurposeName()) {
                case "인터넷" -> pc.add(
                        new UsageStatusResponse.PcUsage(
                                usage.getId(),
                                usage.getSeat(),
                                usage.getName(),
                                usage.getStartAt().toLocalTime().withNano(0),
                                usage.getEndAt().toLocalTime().withNano(0),
                                getRemainMinutes(usage, now)
                        )
                );

                case "탁구" -> pingPong = toRoomUsage(usage, now);

                case "노래방" -> sing = toRoomUsage(usage, now);
            }
        }

        pc.sort(Comparator.comparing(UsageStatusResponse.PcUsage::seat));

        return new UsageStatusResponse(
                new UsageStatusResponse.UsageStatus(pc, pingPong, sing)
        );
    }

    /**
     * 팀 전체 이름·참가 인원·공통 종료 시각과 남은 시간을 노래방·탁구 응답으로 변환한다.
     */
    private UsageStatusResponse.RoomUsage toRoomUsage(
            PurposeUsage usage,
            LocalDateTime now
    ) {
        return new UsageStatusResponse.RoomUsage(
                usage.getId(),
                usage.getName(),
                usage.getCount(),
                usage.getStartAt().toLocalTime().withNano(0),
                usage.getEndAt().toLocalTime().withNano(0),
                getRemainMinutes(usage, now)
        );
    }

    /**
     * 공통 종료 시각까지 남은 시간을 분 단위로 올림하고 음수는 0으로 반환한다.
     */
    private long getRemainMinutes(
            PurposeUsage usage,
            LocalDateTime now
    ) {
        long seconds = Duration.between(now, usage.getEndAt()).getSeconds();

        return Math.max(0, (seconds + 59) / 60);
    }
}
