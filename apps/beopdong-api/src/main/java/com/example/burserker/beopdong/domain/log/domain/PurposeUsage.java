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

    public static PurposeUsage createPc(Purpose purpose, Log log, int seat, String usePurpose, LocalDateTime now) {
        PurposeUsage usage = PurposeUsage.builder().purpose(purpose).seat(seat).usePurpose(usePurpose)
                .startAt(now).endAt(now.plusMinutes(40)).build();
        usage.addMember(log, now);
        return usage;
    }

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

    private void addMember(Log log, LocalDateTime now) {
        members.add(PurposeUsageMember.create(this, log, now));
        count++;
    }

    public String getName() {
        return members.stream().map(member -> member.getLog().getName()).collect(Collectors.joining(", "));
    }

    public void complete() {
        this.completed = true;
    }
}
