package org.oagi.score.gateway.http.api.context_management.business_context.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextAssignmentId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextValueId;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextCommandRepository;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BizCtxRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BizCtxValueRecord;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

public class JooqBusinessContextCommandRepository extends JooqBaseRepository implements BusinessContextCommandRepository {

    public JooqBusinessContextCommandRepository(DSLContext dslContext, ScoreUser requester,
                                                RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public BusinessContextId create(String name) {
        LocalDateTime timestamp = LocalDateTime.now();

        BizCtxRecord record = new BizCtxRecord();

        record.setGuid(randomGuid());
        record.setName(name);
        record.setCreatedBy(valueOf(requester().userId()));
        record.setLastUpdatedBy(valueOf(requester().userId()));
        record.setCreationTimestamp(timestamp);
        record.setLastUpdateTimestamp(timestamp);

        return new BusinessContextId(
                dslContext().insertInto(BIZ_CTX)
                        .set(record)
                        .returning(BIZ_CTX.BIZ_CTX_ID)
                        .fetchOne().getBizCtxId().toBigInteger()
        );
    }

    @Override
    public boolean update(BusinessContextId businessContextId, String name) {

        LocalDateTime timestamp = LocalDateTime.now();

        int numOfUpdatedRecords = dslContext().update(BIZ_CTX)
                .set(BIZ_CTX.NAME, name)
                .set(BIZ_CTX.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(BIZ_CTX.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(BIZ_CTX.BIZ_CTX_ID.eq(valueOf(businessContextId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public int delete(List<BusinessContextId> businessContextIds) {

        dslContext().deleteFrom(BIZ_CTX_VALUE)
                .where(BIZ_CTX_VALUE.BIZ_CTX_ID.in(valueOf(businessContextIds)))
                .execute();

        int numOfDeletedRecords = dslContext().deleteFrom(BIZ_CTX)
                .where(BIZ_CTX.BIZ_CTX_ID.in(valueOf(businessContextIds)))
                .execute();
        return numOfDeletedRecords;
    }

    @Override
    public BusinessContextValueId createValue(BusinessContextId businessContextId, ContextSchemeValueId contextSchemeValueId) {
        BizCtxValueRecord valueRecord = new BizCtxValueRecord();

        valueRecord.setBizCtxId(valueOf(businessContextId));
        valueRecord.setCtxSchemeValueId(valueOf(contextSchemeValueId));

        return new BusinessContextValueId(
                dslContext().insertInto(BIZ_CTX_VALUE)
                        .set(valueRecord)
                        .returning(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID)
                        .fetchOne().getBizCtxValueId().toBigInteger()
        );
    }

    @Override
    public boolean updateValue(BusinessContextValueId businessContextValueId, ContextSchemeValueId contextSchemeValueId) {
        int numOfUpdatedRecords = dslContext().update(BIZ_CTX_VALUE)
                .set(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID, valueOf(contextSchemeValueId))
                .where(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID.eq(valueOf(businessContextValueId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean deleteValue(BusinessContextValueId businessContextValueId) {
        int numOfDeletedRecords = dslContext().deleteFrom(BIZ_CTX_VALUE)
                .where(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID.eq(valueOf(businessContextValueId)))
                .execute();
        return numOfDeletedRecords == 1;
    }

    @Override
    public BusinessContextAssignmentId createAssignment(BusinessContextId businessContextId, TopLevelAsbiepId topLevelAsbiepId) {
        return new BusinessContextAssignmentId(
                dslContext().insertInto(BIZ_CTX_ASSIGNMENT)
                        .set(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID, valueOf(topLevelAsbiepId))
                        .set(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID, valueOf(businessContextId))
                        .returning(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ASSIGNMENT_ID)
                        .fetchOne().getBizCtxAssignmentId().toBigInteger()
        );
    }

    @Override
    public boolean deleteAssignment(BusinessContextId businessContextId, TopLevelAsbiepId topLevelAsbiepId) {
        int numOfDeletedRecords = dslContext().deleteFrom(BIZ_CTX_ASSIGNMENT)
                .where(and(
                        BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)),
                        BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(valueOf(businessContextId))
                ))
                .execute();
        return numOfDeletedRecords == 1;
    }

    @Override
    public int deleteAssignmentList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        int numOfDeletedRecords = dslContext().deleteFrom(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList)))
                .execute();
        return numOfDeletedRecords;
    }
}
