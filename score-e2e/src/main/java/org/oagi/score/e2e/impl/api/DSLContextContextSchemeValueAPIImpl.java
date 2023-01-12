package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.ContextSchemeValueAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.CtxSchemeValueRecord;
import org.oagi.score.e2e.obj.ContextSchemeObject;
import org.oagi.score.e2e.obj.ContextSchemeValueObject;

import java.math.BigInteger;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.CTX_SCHEME_VALUE;

public class DSLContextContextSchemeValueAPIImpl implements ContextSchemeValueAPI {

    private final DSLContext dslContext;

    public DSLContextContextSchemeValueAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public ContextSchemeValueObject getContextSchemeValueById(BigInteger contextSchemeValueId) {
        CtxSchemeValueRecord ctxSchemeValue = dslContext.selectFrom(CTX_SCHEME_VALUE)
                .where(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID.eq(ULong.valueOf(contextSchemeValueId)))
                .fetchOne();
        return mapper(ctxSchemeValue);
    }

    private ContextSchemeValueObject mapper(CtxSchemeValueRecord ctxSchemeValue) {
        ContextSchemeValueObject contextSchemeValue = new ContextSchemeValueObject();
        contextSchemeValue.setContextSchemeValueId(ctxSchemeValue.getCtxSchemeValueId().toBigInteger());
        contextSchemeValue.setGuid(ctxSchemeValue.getGuid());
        contextSchemeValue.setValue(ctxSchemeValue.getValue());
        contextSchemeValue.setMeaning(ctxSchemeValue.getMeaning());
        contextSchemeValue.setOwnerContextSchemeId(ctxSchemeValue.getOwnerCtxSchemeId().toBigInteger());
        return contextSchemeValue;
    }

    @Override
    public ContextSchemeValueObject createRandomContextSchemeValue(ContextSchemeObject contextScheme) {
        ContextSchemeValueObject contextSchemeValue =
                ContextSchemeValueObject.createRandomContextSchemeValue(contextScheme);

        CtxSchemeValueRecord ctxSchemeValue = new CtxSchemeValueRecord();
        ctxSchemeValue.setGuid(contextSchemeValue.getGuid());
        ctxSchemeValue.setValue(contextSchemeValue.getValue());
        ctxSchemeValue.setMeaning(contextSchemeValue.getMeaning());
        ctxSchemeValue.setOwnerCtxSchemeId(ULong.valueOf(contextSchemeValue.getOwnerContextSchemeId()));

        ULong ctxSchemeValueId = dslContext.insertInto(CTX_SCHEME_VALUE)
                .set(ctxSchemeValue)
                .returning(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID)
                .fetchOne().getCtxSchemeValueId();

        contextSchemeValue.setContextSchemeValueId(ctxSchemeValueId.toBigInteger());
        return contextSchemeValue;
    }

    @Override
    public void deleteContextSchemeValueById(BigInteger valueId) {
        ULong valueIdLong = ULong.valueOf(valueId);
        dslContext.deleteFrom(CTX_SCHEME_VALUE).where(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID.eq(valueIdLong)).execute();
    }
}
