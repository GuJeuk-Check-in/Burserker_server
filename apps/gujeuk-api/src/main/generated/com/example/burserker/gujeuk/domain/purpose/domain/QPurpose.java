package com.example.burserker.gujeuk.domain.purpose.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPurpose is a Querydsl query type for Purpose
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPurpose extends EntityPathBase<Purpose> {

    private static final long serialVersionUID = 1335426247L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPurpose purpose = new QPurpose("purpose");

    public final com.example.burserker.gujeuk.global.entity.QBaseIdEntity _super = new com.example.burserker.gujeuk.global.entity.QBaseIdEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final com.example.burserker.gujeuk.domain.organ.domain.QOrgan organ;

    public final NumberPath<Integer> purposeIndex = createNumber("purposeIndex", Integer.class);

    public final StringPath purposeName = createString("purposeName");

    public final BooleanPath reservationStatus = createBoolean("reservationStatus");

    public QPurpose(String variable) {
        this(Purpose.class, forVariable(variable), INITS);
    }

    public QPurpose(Path<? extends Purpose> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPurpose(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPurpose(PathMetadata metadata, PathInits inits) {
        this(Purpose.class, metadata, inits);
    }

    public QPurpose(Class<? extends Purpose> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organ = inits.isInitialized("organ") ? new com.example.burserker.gujeuk.domain.organ.domain.QOrgan(forProperty("organ")) : null;
    }

}

