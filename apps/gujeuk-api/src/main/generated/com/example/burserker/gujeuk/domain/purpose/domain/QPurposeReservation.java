package com.example.burserker.gujeuk.domain.purpose.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPurposeReservation is a Querydsl query type for PurposeReservation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPurposeReservation extends EntityPathBase<PurposeReservation> {

    private static final long serialVersionUID = 1636301957L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPurposeReservation purposeReservation = new QPurposeReservation("purposeReservation");

    public final com.example.burserker.gujeuk.global.entity.QBaseIdEntity _super = new com.example.burserker.gujeuk.global.entity.QBaseIdEntity(this);

    public final NumberPath<Integer> femaleCount = createNumber("femaleCount", Integer.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final NumberPath<Integer> maleCount = createNumber("maleCount", Integer.class);

    public final StringPath name = createString("name");

    public final QPurpose purpose;

    public final StringPath representativePhone = createString("representativePhone");

    public final DatePath<java.time.LocalDate> reservationDate = createDate("reservationDate", java.time.LocalDate.class);

    public final EnumPath<com.example.burserker.gujeuk.domain.purpose.enums.ReservationTime> startTime = createEnum("startTime", com.example.burserker.gujeuk.domain.purpose.enums.ReservationTime.class);

    public final com.example.burserker.gujeuk.domain.user.domain.QUser user;

    public QPurposeReservation(String variable) {
        this(PurposeReservation.class, forVariable(variable), INITS);
    }

    public QPurposeReservation(Path<? extends PurposeReservation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPurposeReservation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPurposeReservation(PathMetadata metadata, PathInits inits) {
        this(PurposeReservation.class, metadata, inits);
    }

    public QPurposeReservation(Class<? extends PurposeReservation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.purpose = inits.isInitialized("purpose") ? new QPurpose(forProperty("purpose"), inits.get("purpose")) : null;
        this.user = inits.isInitialized("user") ? new com.example.burserker.gujeuk.domain.user.domain.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

