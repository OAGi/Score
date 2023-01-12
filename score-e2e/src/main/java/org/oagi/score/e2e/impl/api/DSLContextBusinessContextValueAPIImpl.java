package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.BusinessContextValueAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.BizCtxValueRecord;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.BusinessContextValueObject;
import org.oagi.score.e2e.obj.ContextSchemeValueObject;

import java.math.BigInteger;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.BIZ_CTX_VALUE;

public class DSLContextBusinessContextValueAPIImpl implements BusinessContextValueAPI {

    private final DSLContext dslContext;

    public DSLContextBusinessContextValueAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public BusinessContextValueObject createRandomBusinessContextValue(
            BusinessContextObject businessContext, ContextSchemeValueObject contextSchemeValue) {
        BusinessContextValueObject businessContextValue =
                BusinessContextValueObject.createBusinessContextValue(businessContext, contextSchemeValue);

        BizCtxValueRecord bizCtxValueRecord = new BizCtxValueRecord();
        bizCtxValueRecord.setBizCtxId(ULong.valueOf(businessContextValue.getBusinessContextId()));
        bizCtxValueRecord.setCtxSchemeValueId(ULong.valueOf(businessContextValue.getContextSchemeValueId()));
        ULong ctxValueId = dslContext.insertInto(BIZ_CTX_VALUE)
                .set(bizCtxValueRecord)
                .returning(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID)
                .fetchOne().getBizCtxValueId();
        businessContextValue.setBusinessContextValueId(ctxValueId.toBigInteger());

        return businessContextValue;
    }

    @Override
    public void deleteBusinessContextValueById(BigInteger businessContextValueId) {
        ULong bcValueIdLong = ULong.valueOf(businessContextValueId);
        dslContext.deleteFrom(BIZ_CTX_VALUE).where(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID.eq(bcValueIdLong)).execute();
    }
}
