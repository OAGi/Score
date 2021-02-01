package org.oagi.score.repo.api.impl.jooq.businesscontext;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businesscontext.ContextCategoryWriteRepository;
import org.oagi.score.repo.api.businesscontext.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CtxCategoryRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CTX_CATEGORY;
import static org.oagi.score.repo.api.impl.jooq.utils.ScoreGuidUtils.randomGuid;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqContextCategoryWriteRepository
        extends JooqScoreRepository
        implements ContextCategoryWriteRepository {

    public JooqContextCategoryWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateContextCategoryResponse createContextCategory(
            CreateContextCategoryRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        CtxCategoryRecord record = new CtxCategoryRecord();

        record.setGuid(randomGuid());
        record.setName(request.getName());
        record.setDescription(request.getDescription());
        record.setCreatedBy(requesterUserId);
        record.setLastUpdatedBy(requesterUserId);
        record.setCreationTimestamp(timestamp);
        record.setLastUpdateTimestamp(timestamp);

        BigInteger contextCategoryId = dslContext().insertInto(CTX_CATEGORY)
                .set(record)
                .returning(CTX_CATEGORY.CTX_CATEGORY_ID)
                .fetchOne().getCtxCategoryId().toBigInteger();

        return new CreateContextCategoryResponse(contextCategoryId);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public UpdateContextCategoryResponse updateContextCategory(
            UpdateContextCategoryRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        CtxCategoryRecord record = dslContext().selectFrom(CTX_CATEGORY)
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(ULong.valueOf(request.getContextCategoryId())))
                .fetchOptional().orElse(null);
        if (record == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }

        List<Field<?>> changedField = new ArrayList();
        if (!StringUtils.equals(request.getName(), record.getName())) {
            record.setName(request.getName());
            changedField.add(CTX_CATEGORY.NAME);
        }
        if (!StringUtils.equals(request.getDescription(), record.getDescription())) {
            record.setDescription(request.getDescription());
            changedField.add(CTX_CATEGORY.DESCRIPTION);
        }
        if (!changedField.isEmpty()) {
            record.setLastUpdatedBy(requesterUserId);
            changedField.add(CTX_CATEGORY.LAST_UPDATED_BY);

            record.setLastUpdateTimestamp(timestamp);
            changedField.add(CTX_CATEGORY.LAST_UPDATE_TIMESTAMP);

            int affectedRows = record.update(changedField);
            if (affectedRows != 1) {
                throw new ScoreDataAccessException(new IllegalStateException());
            }
        }

        return new UpdateContextCategoryResponse(
                record.getCtxCategoryId().toBigInteger(),
                !changedField.isEmpty());
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public DeleteContextCategoryResponse deleteContextCategory(
            DeleteContextCategoryRequest request) throws ScoreDataAccessException {

        if (request.getContextCategoryIdList() == null || request.getContextCategoryIdList().isEmpty()) {
            throw new IllegalArgumentException("Not allow with empty parameters.");
        }

        List<ULong> contextCategoryIdList =
                request.getContextCategoryIdList().stream()
                        .map(e -> ULong.valueOf(e)).collect(Collectors.toList());

        int affectedRows = dslContext().deleteFrom(CTX_CATEGORY)
                .where(
                        contextCategoryIdList.size() == 1 ?
                                CTX_CATEGORY.CTX_CATEGORY_ID.eq(contextCategoryIdList.get(0)) :
                                CTX_CATEGORY.CTX_CATEGORY_ID.in(contextCategoryIdList)
                )
                .execute();
        if (affectedRows < 1) {
            throw new ScoreDataAccessException(new IllegalStateException());
        }

        return new DeleteContextCategoryResponse(request.getContextCategoryIdList());
    }
}
