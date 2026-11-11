package com.example.burserker.beopdong.domain.log.domain;

import com.example.burserker.beopdong.domain.check_in.exception.InvalidFormPurposeException;
import com.example.burserker.beopdong.domain.check_in.exception.UsageAlreadyActiveException;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.global.entity.BaseIdEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurposeUsage extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purpose_id", nullable = false)
    private Purpose purpose;

    private Integer seat;

    private String usePurpose;

    @Column(nullable = false)
    private int count;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private boolean completed;

    @Builder.Default
    @OneToMany(mappedBy = "purposeUsage", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("joinedAt ASC, id ASC")
    private List<PurposeUsageMember> members = new ArrayList<>();

    /**
     * 컴퓨터 한 좌석에 첫 참가자를 등록하고 신청 시각부터 40분의 회차를 생성한다.
     */
    public static PurposeUsage createPc(Purpose purpose, Log log, int seat, String usePurpose, LocalDateTime now) {
        PurposeUsage usage = PurposeUsage.builder().purpose(purpose).seat(seat).usePurpose(usePurpose)
                .startAt(now).endAt(now.plusMinutes(40)).build();
        usage.addMember(log, now);
        return usage;
    }

    /**
     * 노래방은 20분, 탁구는 40분의 회차를 만들고 첫 참가자를 등록한다.
     */
    public static PurposeUsage createRoom(Purpose purpose, Log log, LocalDateTime now) {
        int minutes;
        if ("노래방".equals(purpose.getPurposeName())) {
            minutes = 20;
        } else if ("탁구".equals(purpose.getPurposeName())) {
            minutes = 40;
        } else {
            throw InvalidFormPurposeException.EXCEPTION;
        }
        PurposeUsage usage = PurposeUsage.builder().purpose(purpose).startAt(now).endAt(now.plusMinutes(minutes)).build();
        usage.addMember(log, now);
        return usage;
    }

    /**
     * 정원과 중복 참가를 확인해 합류시킨다. 노래방만 최초 시작부터 30분으로 종료 시각을 변경한다.
     */
    public void join(Log log, LocalDateTime now) {
        int capacity;
        if ("노래방".equals(purpose.getPurposeName())) {
            capacity = 2;
        } else if ("탁구".equals(purpose.getPurposeName())) {
            capacity = 4;
        } else {
            throw InvalidFormPurposeException.EXCEPTION;
        }
        if (completed || !now.isBefore(endAt) || count >= capacity
                || members.stream().anyMatch(member -> member.getLog().getUser().getId().equals(log.getUser().getId()))) {
            throw UsageAlreadyActiveException.EXCEPTION;
        }
        addMember(log, now);
        if ("노래방".equals(purpose.getPurposeName())) {
            this.endAt = startAt.plusMinutes(30);
        }
    }

    /**
     * 참가자의 방명록과 합류 시각을 연결하고 회차 인원을 한 명 늘린다.
     */
    private void addMember(Log log, LocalDateTime now) {
        members.add(PurposeUsageMember.create(this, log, now));
        count++;
    }

    /**
     * 현재 참가자 전원의 방명록 이름을 순서대로 연결하여 기존 문자열 응답 형식을 유지한다.
     */
    public String getName() {
        return members.stream().map(member -> member.getLog().getName()).collect(Collectors.joining(", "));
    }

    /**
     * 완료 이력이 생성된 회차임을 표시한다. 참가자 연결은 이력 확인을 위해 보존한다.
     */
    public void complete() {
        this.completed = true;
    }
}
