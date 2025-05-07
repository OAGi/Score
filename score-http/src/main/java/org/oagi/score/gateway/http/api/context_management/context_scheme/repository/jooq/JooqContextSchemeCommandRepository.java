package org.oagi.score.gateway.http.api.context_management.context_scheme.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListId;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.ContextSchemeCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.CtxSchemeRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.CtxSchemeValueRecord;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.CTX_SCHEME;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.CTX_SCHEME_VALUE;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

public class JooqContextSchemeCommandRepository extends JooqBaseRepository implements ContextSchemeCommandRepository {

    public JooqContextSchemeCommandRepository(DSLContext dslContext, ScoreUser requester,
                                              RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public ContextSchemeId create(ContextCategoryId contextCategoryId,
                                  CodeListId codeListId,
                                  String schemeId, String schemeName,
                                  String schemeAgencyId, String schemeVersionId,
                                  String description) {

        LocalDateTime timestamp = LocalDateTime.now();

        CtxSchemeRecord record = new CtxSchemeRecord();
        record.setGuid(randomGuid());
        record.setCtxCategoryId(valueOf(contextCategoryId));
        if (codeListId != null) {
            record.setCodeListId(valueOf(codeListId));
        }
        record.setSchemeId(schemeId);
        record.setSchemeName(schemeName);
        record.setSchemeAgencyId(schemeAgencyId);
        record.setSchemeVersionId(schemeVersionId);
        record.setDescription(description);
        record.setCreatedBy(valueOf(requester().userId()));
        record.setLastUpdatedBy(valueOf(requester().userId()));
        record.setCreationTimestamp(timestamp);
        record.setLastUpdateTimestamp(timestamp);

        return new ContextSchemeId(
                dslContext().insertInto(CTX_SCHEME)
                        .set(record)
                        .returning(CTX_SCHEME.CTX_SCHEME_ID)
                        .fetchOne().getCtxSchemeId().toBigInteger()
        );
    }

    @Override
    public boolean update(ContextSchemeId contextSchemeId,
                          ContextCategoryId contextCategoryId, CodeListId codeListId,
                          String schemeId, String schemeName,
                          String schemeAgencyId, String schemeVersionId,
                          String description) {

        LocalDateTime timestamp = LocalDateTime.now();

        int numOfUpdatedRecords = dslContext().update(CTX_SCHEME)
                .set(CTX_SCHEME.SCHEME_ID, schemeId)
                .set(CTX_SCHEME.SCHEME_NAME, schemeName)
                .set(CTX_SCHEME.SCHEME_AGENCY_ID, schemeAgencyId)
                .set(CTX_SCHEME.SCHEME_VERSION_ID, schemeVersionId)
                .set(CTX_SCHEME.DESCRIPTION, description)
                .set(CTX_SCHEME.SCHEME_ID, schemeId)
                .set(CTX_SCHEME.CTX_CATEGORY_ID, valueOf(contextCategoryId))
                .set(CTX_SCHEME.CODE_LIST_ID, (codeListId != null) ? valueOf(codeListId) : null)
                .set(CTX_SCHEME.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(CTX_SCHEME.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(CTX_SCHEME.CTX_SCHEME_ID.eq(valueOf(contextSchemeId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public int delete(Collection<ContextSchemeId> contextSchemeIdList) {

        dslContext().deleteFrom(CTX_SCHEME_VALUE)
                .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.in(valueOf(contextSchemeIdList)))
                .execute();

        int numOfDeletedRecords = dslContext().deleteFrom(CTX_SCHEME)
                .where(CTX_SCHEME.CTX_SCHEME_ID.in(valueOf(contextSchemeIdList)))
                .execute();
        return numOfDeletedRecords;
    }

    @Override
    public ContextSchemeValueId createValue(ContextSchemeId contextSchemeId, String value, String meaning) {

        CtxSchemeValueRecord valueRecord = new CtxSchemeValueRecord();

        valueRecord.setGuid(randomGuid());
        valueRecord.setValue(value);
        valueRecord.setMeaning(meaning);
        valueRecord.setOwnerCtxSchemeId(valueOf(contextSchemeId));

        return new ContextSchemeValueId(
                dslContext().insertInto(CTX_SCHEME_VALUE)
                        .set(valueRecord)
                        .returning(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID)
                        .fetchOne().getCtxSchemeValueId().toBigInteger()
        );
    }

    @Override
    public boolean updateValue(ContextSchemeValueId contextSchemeValueId, String value, String meaning) {

        int numOfUpdatedRecords = dslContext().update(CTX_SCHEME_VALUE)
                .set(CTX_SCHEME_VALUE.VALUE, value)
                .set(CTX_SCHEME_VALUE.MEANING, meaning)
                .where(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID.eq(valueOf(contextSchemeValueId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean deleteValue(ContextSchemeValueId contextSchemeValueId) {

        int numOfDeletedRecords = dslContext().deleteFrom(CTX_SCHEME_VALUE)
                .where(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID.eq(valueOf(contextSchemeValueId)))
                .execute();
        return numOfDeletedRecords == 1;
    }

}
