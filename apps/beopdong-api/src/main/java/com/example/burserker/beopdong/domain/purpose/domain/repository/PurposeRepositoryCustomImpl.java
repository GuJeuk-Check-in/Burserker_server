package com.example.burserker.beopdong.domain.purpose.domain.repository;

import com.example.burserker.beopdong.domain.purpose.domain.QPurpose;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PurposeRepositoryCustomImpl
        implements PurposeRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QPurpose qPurpose = QPurpose.purpose;

    @Override
    public int findMaxPurposeIndexByOrganId(Long organId) {
        Integer max = jpaQueryFactory
                .select(qPurpose.purposeIndex.max())
                .where(qPurpose.organ.id.eq(organId))
                .from(qPurpose)
                .fetchOne();

        return max == null ? 0 : max;
    }
}