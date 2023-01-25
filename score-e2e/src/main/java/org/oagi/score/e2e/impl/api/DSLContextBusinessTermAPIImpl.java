package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.BusinessTermAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.BusinessTermRecord;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessTermObject;

import java.math.BigInteger;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.*;

public class DSLContextBusinessTermAPIImpl implements BusinessTermAPI {
    private final DSLContext dslContext;

    private final APIFactory apiFactory;

    public DSLContextBusinessTermAPIImpl(DSLContext dslContext, APIFactory apiFactory) {
        this.dslContext = dslContext;
        this.apiFactory = apiFactory;
    }

    @Override
    public BusinessTermObject getBusinessTermByName(String businessTermName) {
        BusinessTermRecord bizTerm = dslContext.selectFrom(BUSINESS_TERM)
                .where(BUSINESS_TERM.BUSINESS_TERM_.eq(businessTermName))
                .fetchOne();

        BusinessTermObject businessTerm = new BusinessTermObject();
        businessTerm.setBusinessTermId(bizTerm.getBusinessTermId().toBigInteger());
        businessTerm.setGuid(bizTerm.getGuid());
        businessTerm.setBusinessTerm(bizTerm.getBusinessTerm());
        businessTerm.setExternalReferenceId(bizTerm.getExternalRefId());
        businessTerm.setExternalReferenceUri(bizTerm.getExternalRefUri());
        businessTerm.setDefinition(bizTerm.getDefinition());
        businessTerm.setComment(bizTerm.getComment());
        businessTerm.setCreatedBy(bizTerm.getCreatedBy().toBigInteger());
        businessTerm.setLastUpdatedBy(bizTerm.getLastUpdatedBy().toBigInteger());
        businessTerm.setLastUpdateTimestamp(bizTerm.getLastUpdateTimestamp());
        return businessTerm;
    }

    @Override
    public BusinessTermObject createRandomBusinessTerm(AppUserObject creator) {
        return createRandomBusinessTerm(creator, "bt");
    }

    @Override
    public BusinessTermObject createRandomBusinessTerm(AppUserObject creator, String namePrefix) {
        BusinessTermObject randomBusinessTerm =
                BusinessTermObject.createRandomBusinessTerm(creator, namePrefix);
        return createBusinessTerm(randomBusinessTerm);
    }

    @Override
    public BusinessTermObject createBusinessTerm(BusinessTermObject businessTerm) {
        BusinessTermRecord bizTermRecord = new BusinessTermRecord();
        bizTermRecord.setGuid(businessTerm.getGuid());
        bizTermRecord.setExternalRefUri(businessTerm.getExternalReferenceUri());
        bizTermRecord.setExternalRefId(businessTerm.getExternalReferenceId());
        bizTermRecord.setBusinessTerm(businessTerm.getBusinessTerm());
        bizTermRecord.setDefinition(businessTerm.getDefinition());
        bizTermRecord.setComment(businessTerm.getComment());
        bizTermRecord.setCreatedBy(ULong.valueOf(businessTerm.getCreatedBy()));
        bizTermRecord.setLastUpdatedBy(ULong.valueOf(businessTerm.getLastUpdatedBy()));
        bizTermRecord.setLastUpdateTimestamp(businessTerm.getLastUpdateTimestamp());

        ULong businessTermId = dslContext.insertInto(BUSINESS_TERM)
                .set(bizTermRecord)
                .returning(BUSINESS_TERM.BUSINESS_TERM_ID)
                .fetchOne().getBusinessTermId();

        businessTerm.setBusinessTermId(businessTermId.toBigInteger());
        return businessTerm;
    }

    @Override
    public void deleteBusinessTermById(BigInteger businessTermId) {
        ULong btIDLong = ULong.valueOf(businessTermId);
        dslContext.deleteFrom(BUSINESS_TERM).where(BUSINESS_TERM.BUSINESS_TERM_ID.eq(btIDLong)).execute();
    }

    private void deleteBusinessContextValuesAndAssignments(ULong bizCtxId) {
        dslContext.deleteFrom(BIZ_CTX_ASSIGNMENT).where(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(bizCtxId)).execute();
        dslContext.deleteFrom(BIZ_CTX_VALUE)
                .where(BIZ_CTX_VALUE.BIZ_CTX_ID.eq(bizCtxId))
                .execute();
        dslContext.deleteFrom(BIZ_CTX).where(BIZ_CTX.BIZ_CTX_ID.eq(bizCtxId)).execute();
    }

}
