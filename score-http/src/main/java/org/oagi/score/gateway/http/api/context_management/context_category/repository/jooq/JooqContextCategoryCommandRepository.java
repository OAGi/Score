package org.oagi.score.gateway.http.api.context_management.context_category.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.UpdateSetMoreStep;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryDetailsRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.ContextCategoryCommandRepository;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.ContextCategoryQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.CtxCategoryRecord;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.CTX_CATEGORY;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;
import static org.springframework.util.StringUtils.hasLength;

public class JooqContextCategoryCommandRepository extends JooqBaseRepository implements ContextCategoryCommandRepository {

    private final ContextCategoryQueryRepository contextCategoryQueryRepository;

    public JooqContextCategoryCommandRepository(DSLContext dslContext, ScoreUser requester,
                                                RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);

        this.contextCategoryQueryRepository = repositoryFactory.contextCategoryQueryRepository(requester);
    }

    @Override
    public ContextCategoryId create(String name, String description) {
        if (!hasLength(name)) {
            throw new IllegalArgumentException("Name must not be empty.");
        }
        LocalDateTime timestamp = LocalDateTime.now();

        CtxCategoryRecord record = new CtxCategoryRecord();

        record.setGuid(randomGuid());
        record.setName(name);
        record.setDescription(description);
        record.setCreatedBy(valueOf(requester().userId()));
        record.setLastUpdatedBy(valueOf(requester().userId()));
        record.setCreationTimestamp(timestamp);
        record.setLastUpdateTimestamp(timestamp);

        return new ContextCategoryId(
                dslContext().insertInto(CTX_CATEGORY)
                        .set(record)
                        .returning(CTX_CATEGORY.CTX_CATEGORY_ID)
                        .fetchOne().getCtxCategoryId().toBigInteger()
        );
    }

    @Override
    public boolean update(ContextCategoryId contextCategoryId, String name, String description) {

        ContextCategoryDetailsRecord details = contextCategoryQueryRepository.getContextCategoryDetails(contextCategoryId);
        if (details == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }

        var update = dslContext().update(CTX_CATEGORY);
        UpdateSetMoreStep<CtxCategoryRecord> step = null;

        if (!StringUtils.equals(name, details.name())) {
            if (StringUtils.hasLength(name)) {
                step = update.set(CTX_CATEGORY.NAME, name);
            } else {
                step = update.setNull(CTX_CATEGORY.NAME);
            }
        }
        if (!StringUtils.equals(description, details.description())) {
            if (StringUtils.hasLength(name)) {
                step = update.set(CTX_CATEGORY.DESCRIPTION, description);
            } else {
                step = update.setNull(CTX_CATEGORY.DESCRIPTION);
            }
        }

        if (step == null) {
            return false;
        }

        LocalDateTime timestamp = LocalDateTime.now();
        int numOfUpdatedRecords = step.set(CTX_CATEGORY.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(CTX_CATEGORY.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(valueOf(contextCategoryId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean delete(ContextCategoryId contextCategoryId) {
        return delete(Arrays.asList(contextCategoryId)) != 1;
    }

    @Override
    public int delete(Collection<ContextCategoryId> contextCategoryIdList) {
        if (contextCategoryIdList == null || contextCategoryIdList.isEmpty()) {
            return 0;
        }

        int numOfDeletedRecords = dslContext().deleteFrom(CTX_CATEGORY)
                .where(
                        contextCategoryIdList.size() == 1 ?
                                CTX_CATEGORY.CTX_CATEGORY_ID.eq(valueOf(contextCategoryIdList.iterator().next())) :
                                CTX_CATEGORY.CTX_CATEGORY_ID.in(valueOf(contextCategoryIdList))
                )
                .execute();
        return numOfDeletedRecords;
    }

}
