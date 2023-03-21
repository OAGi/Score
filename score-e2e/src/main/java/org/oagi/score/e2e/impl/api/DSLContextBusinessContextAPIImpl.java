package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.BusinessContextAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.BizCtxRecord;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;

import java.math.BigInteger;
import java.util.List;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.*;

public class DSLContextBusinessContextAPIImpl implements BusinessContextAPI {

    private final DSLContext dslContext;

    private final APIFactory apiFactory;

    public DSLContextBusinessContextAPIImpl(DSLContext dslContext, APIFactory apiFactory) {
        this.dslContext = dslContext;
        this.apiFactory = apiFactory;
    }

    @Override
    public BusinessContextObject getBusinessContextByName(String businessContextName) {
        BizCtxRecord bizCtx = dslContext.selectFrom(BIZ_CTX)
                .where(BIZ_CTX.NAME.eq(businessContextName))
                .fetchOne();

        BusinessContextObject businessContext = new BusinessContextObject();
        businessContext.setBusinessContextId(bizCtx.getBizCtxId().toBigInteger());
        businessContext.setGuid(bizCtx.getGuid());
        businessContext.setName(bizCtx.getName());
        businessContext.setCreatedBy(bizCtx.getCreatedBy().toBigInteger());
        businessContext.setLastUpdatedBy(bizCtx.getLastUpdatedBy().toBigInteger());
        businessContext.setCreationTimestamp(bizCtx.getCreationTimestamp());
        businessContext.setLastUpdateTimestamp(bizCtx.getLastUpdateTimestamp());
        return businessContext;
    }

    @Override
    public BusinessContextObject createRandomBusinessContext(AppUserObject creator) {
        return createRandomBusinessContext(creator, "bc");
    }

    @Override
    public BusinessContextObject createRandomBusinessContext(AppUserObject creator, String namePrefix) {
        BusinessContextObject randomBusinessContext =
                BusinessContextObject.createRandomBusinessContext(creator, namePrefix);
        return createBusinessContext(randomBusinessContext);
    }

    @Override
    public BusinessContextObject createBusinessContext(BusinessContextObject businessContext) {
        BizCtxRecord bizCtxRecord = new BizCtxRecord();
        bizCtxRecord.setGuid(businessContext.getGuid());
        bizCtxRecord.setName(businessContext.getName());
        bizCtxRecord.setCreatedBy(ULong.valueOf(businessContext.getCreatedBy()));
        bizCtxRecord.setLastUpdatedBy(ULong.valueOf(businessContext.getLastUpdatedBy()));
        bizCtxRecord.setCreationTimestamp(businessContext.getCreationTimestamp());
        bizCtxRecord.setLastUpdateTimestamp(businessContext.getLastUpdateTimestamp());

        ULong businessContextId = dslContext.insertInto(BIZ_CTX)
                .set(bizCtxRecord)
                .returning(BIZ_CTX.BIZ_CTX_ID)
                .fetchOne().getBizCtxId();

        businessContext.setBusinessContextId(businessContextId.toBigInteger());
        return businessContext;
    }

    @Override
    public void deleteBusinessContextById(BigInteger businessContext) {
        ULong bcIDLong = ULong.valueOf(businessContext);
        dslContext.deleteFrom(BIZ_CTX_VALUE)
                .where(BIZ_CTX_VALUE.BIZ_CTX_ID.eq(bcIDLong))
                .execute();

        dslContext.deleteFrom(BIZ_CTX).where(BIZ_CTX.BIZ_CTX_ID.eq(bcIDLong)).execute();
    }

    @Override
    public void deleteRandomBusinessContextData(BusinessContextObject businessContext) {
        ULong bizCtxId = ULong.valueOf(businessContext.getBusinessContextId());

        List<ULong> ctxSchemeValueIdList = dslContext.select(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID)
                .from(BIZ_CTX_VALUE)
                .where(BIZ_CTX_VALUE.BIZ_CTX_ID.eq(bizCtxId))
                .fetchInto(ULong.class);

        deleteBusinessContextValuesAndAssignments(bizCtxId);
        deleteContextSchemeByBCValueId(ctxSchemeValueIdList);
    }

    private void deleteBusinessContextValuesAndAssignments(ULong bizCtxId) {
        dslContext.deleteFrom(BIZ_CTX_ASSIGNMENT).where(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(bizCtxId)).execute();
        dslContext.deleteFrom(BIZ_CTX_VALUE)
                .where(BIZ_CTX_VALUE.BIZ_CTX_ID.eq(bizCtxId))
                .execute();
        dslContext.deleteFrom(BIZ_CTX).where(BIZ_CTX.BIZ_CTX_ID.eq(bizCtxId)).execute();
    }

    private void deleteContextSchemeByBCValueId(List<ULong> ctxSchemeValueIdList) {
        List<ULong> ctxSchemeIdList = dslContext.select(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID)
                .from(CTX_SCHEME_VALUE)
                .where(
                        CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID.in(ctxSchemeValueIdList))
                .fetchInto(ULong.class);

        List<ULong> ctxCategoryIdList = dslContext.select(CTX_SCHEME.CTX_CATEGORY_ID)
                .from(CTX_SCHEME)
                .where(
                        CTX_SCHEME.CTX_SCHEME_ID.in(ctxSchemeIdList))
                .fetchInto(ULong.class);


        deleteSchemaValue(ctxSchemeValueIdList);
        deleteSchema(ctxSchemeIdList);
        deleteContextCategoryBySchemaId(ctxCategoryIdList);

    }

    private void deleteSchema(List<ULong> ctxSchemeIdList) {
        ctxSchemeIdList.forEach(newBCScheme -> {
            dslContext.deleteFrom(CTX_SCHEME).where(CTX_SCHEME.CTX_SCHEME_ID.eq(newBCScheme)).execute();
        });
    }

    private void deleteSchemaValue(List<ULong> ctxSchemeValueIdList) {
        ctxSchemeValueIdList.forEach(schemeValue -> {
            dslContext.deleteFrom(CTX_SCHEME_VALUE).where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(schemeValue)).execute();
        });
    }

    private void deleteContextCategoryBySchemaId(List<ULong> ctxCategoryIdList) {
        ctxCategoryIdList.forEach(newBCCategory -> {
            dslContext.deleteFrom(CTX_CATEGORY).where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(newBCCategory)).execute();
        });
    }
}
