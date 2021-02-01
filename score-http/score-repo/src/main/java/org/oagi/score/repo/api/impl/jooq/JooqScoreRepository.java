package org.oagi.score.repo.api.impl.jooq;

import org.jooq.DSLContext;

import java.math.BigInteger;

public abstract class JooqScoreRepository {

    private final DSLContext dslContext;

    public JooqScoreRepository(DSLContext dslContext) {
        if (dslContext == null) {
            throw new IllegalArgumentException();
        }

        this.dslContext = dslContext;
    }

    public DSLContext dslContext() {
        return this.dslContext;
    }

}
