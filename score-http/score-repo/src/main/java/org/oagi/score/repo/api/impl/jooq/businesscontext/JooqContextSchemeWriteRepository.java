package org.oagi.score.repo.api.impl.jooq.businesscontext;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businesscontext.ContextSchemeWriteRepository;
import org.oagi.score.repo.api.businesscontext.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CtxSchemeRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CtxSchemeValueRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CTX_SCHEME;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CTX_SCHEME_VALUE;
import static org.oagi.score.repo.api.impl.jooq.utils.ScoreGuidUtils.randomGuid;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqContextSchemeWriteRepository
        extends JooqScoreRepository
        implements ContextSchemeWriteRepository {

    public JooqContextSchemeWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateContextSchemeResponse createContextScheme(
            CreateContextSchemeRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        CtxSchemeRecord record = new CtxSchemeRecord();

        record.setGuid(randomGuid());
        record.setSchemeId(request.getSchemeId());
        record.setSchemeName(request.getSchemeName());
        record.setDescription(request.getDescription());
        record.setSchemeAgencyId(request.getSchemeAgencyId());
        record.setSchemeVersionId(request.getSchemeVersionId());
        record.setCtxCategoryId(ULong.valueOf(request.getContextCategoryId()));
        record.setCreatedBy(requesterUserId);
        record.setLastUpdatedBy(requesterUserId);
        record.setCreationTimestamp(timestamp);
        record.setLastUpdateTimestamp(timestamp);

        BigInteger contextSchemeId = dslContext().insertInto(CTX_SCHEME)
                .set(record)
                .returning(CTX_SCHEME.CTX_SCHEME_ID)
                .fetchOne().getCtxSchemeId().toBigInteger();

        CreateContextSchemeResponse response = new CreateContextSchemeResponse(contextSchemeId);

        for (ContextSchemeValue contextSchemeValue : request.getContextSchemeValueList()) {
            contextSchemeValue = createContextSchemeValue(contextSchemeValue, contextSchemeId);
            response.addContextSchemeValue(contextSchemeValue);
        }

        return response;
    }

    private ContextSchemeValue createContextSchemeValue(ContextSchemeValue contextSchemeValue,
                                                BigInteger ownerContextSchemeId) {
        CtxSchemeValueRecord valueRecord = new CtxSchemeValueRecord();

        valueRecord.setGuid(randomGuid());
        valueRecord.setValue(contextSchemeValue.getValue());
        valueRecord.setMeaning(contextSchemeValue.getMeaning());
        valueRecord.setOwnerCtxSchemeId(ULong.valueOf(ownerContextSchemeId));

        BigInteger contextSchemeValueId = dslContext().insertInto(CTX_SCHEME_VALUE)
                        .set(valueRecord)
                        .returning(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID)
                        .fetchOne().getCtxSchemeValueId().toBigInteger();

        contextSchemeValue.setContextSchemeValueId(contextSchemeValueId);
        contextSchemeValue.setGuid(valueRecord.getGuid());
        return contextSchemeValue;
    }

    @Override
    public UpdateContextSchemeResponse updateContextScheme(
            UpdateContextSchemeRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        CtxSchemeRecord record = dslContext().selectFrom(CTX_SCHEME)
                .where(CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(request.getContextSchemeId())))
                .fetchOptional().orElse(null);
        if (record == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }

        List<Field<?>> changedField = new ArrayList();
        if (request.getContextCategoryId() != null &&
                !record.getCtxCategoryId().equals(ULong.valueOf(request.getContextCategoryId()))) {
            record.setCtxCategoryId(ULong.valueOf(request.getContextCategoryId()));
            changedField.add(CTX_SCHEME.CTX_CATEGORY_ID);
        }
        if (request.getCodeListId() != null) {
            record.setCodeListId(ULong.valueOf(request.getCodeListId()));
            changedField.add(CTX_SCHEME.CODE_LIST_ID);
        }
        if (!StringUtils.equals(request.getSchemeId(), record.getSchemeId())) {
            record.setSchemeId(request.getSchemeId());
            changedField.add(CTX_SCHEME.SCHEME_ID);
        }
        if (!StringUtils.equals(request.getSchemeName(), record.getSchemeName())) {
            record.setSchemeName(request.getSchemeName());
            changedField.add(CTX_SCHEME.SCHEME_NAME);
        }
        if (!StringUtils.equals(request.getSchemeAgencyId(), record.getSchemeAgencyId())) {
            record.setSchemeAgencyId(request.getSchemeAgencyId());
            changedField.add(CTX_SCHEME.SCHEME_AGENCY_ID);
        }
        if (!StringUtils.equals(request.getSchemeVersionId(), record.getSchemeVersionId())) {
            record.setSchemeVersionId(request.getSchemeVersionId());
            changedField.add(CTX_SCHEME.SCHEME_VERSION_ID);
        }
        if (!StringUtils.equals(request.getDescription(), record.getDescription())) {
            record.setDescription(request.getDescription());
            changedField.add(CTX_SCHEME.DESCRIPTION);
        }
        if (!changedField.isEmpty()) {
            record.setLastUpdatedBy(requesterUserId);
            changedField.add(CTX_SCHEME.LAST_UPDATED_BY);

            record.setLastUpdateTimestamp(timestamp);
            changedField.add(CTX_SCHEME.LAST_UPDATE_TIMESTAMP);

            int affectedRows = record.update(changedField);
            if (affectedRows != 1) {
                throw new ScoreDataAccessException(new IllegalStateException());
            }
        }

        updateContextSchemeValue(request);

        return new UpdateContextSchemeResponse(
                record.getCtxSchemeId().toBigInteger(),
                !changedField.isEmpty());
    }

    private void updateContextSchemeValue(UpdateContextSchemeRequest request) {
        List<ULong> oldCtxSchemeValueGuidList =
                dslContext().select(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID)
                        .from(CTX_SCHEME_VALUE)
                        .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(ULong.valueOf(request.getContextSchemeId())))
                        .fetch(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID);

        Map<BigInteger, ContextSchemeValue> contextSchemeValues = request.getContextSchemeValueList().stream()
                .filter(e -> e.getContextSchemeValueId() != null)
                .collect(Collectors.toMap(ContextSchemeValue::getContextSchemeValueId, Function.identity()));

        // Delete context scheme values not contained in the request
        dslContext().deleteFrom(CTX_SCHEME_VALUE)
                .where(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID.in(
                        oldCtxSchemeValueGuidList.stream()
                                .filter(e -> !contextSchemeValues.keySet().contains(e.toBigInteger()))
                                .collect(Collectors.toList())
                ))
                .execute();

        for (ContextSchemeValue contextSchemeValue : contextSchemeValues.values()) {
            dslContext().update(CTX_SCHEME_VALUE)
                    .set(CTX_SCHEME_VALUE.VALUE, contextSchemeValue.getValue())
                    .set(CTX_SCHEME_VALUE.MEANING, contextSchemeValue.getMeaning())
                    .where(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID.eq(ULong.valueOf(contextSchemeValue.getContextSchemeValueId())))
                    .execute();
        }

        request.getContextSchemeValueList().stream()
                .filter(e -> e.getContextSchemeValueId() == null).forEach(e -> {
            createContextSchemeValue(e, request.getContextSchemeId());
        });
    }

    @Override
    public DeleteContextSchemeResponse deleteContextScheme(
            DeleteContextSchemeRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        List<BigInteger> contextSchemeIdList = request.getContextSchemeIdList();
        if (contextSchemeIdList == null || contextSchemeIdList.isEmpty()) {
            return new DeleteContextSchemeResponse(Collections.emptyList());
        }

        dslContext().delete(CTX_SCHEME_VALUE)
                .where(
                        contextSchemeIdList.size() == 1 ?
                                CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(ULong.valueOf(contextSchemeIdList.get(0))) :
                                CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.in(
                                        contextSchemeIdList.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                                )
                )
                .execute();
        dslContext().delete(CTX_SCHEME)
                .where(
                        contextSchemeIdList.size() == 1 ?
                                CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(contextSchemeIdList.get(0))) :
                                CTX_SCHEME.CTX_SCHEME_ID.in(
                                        contextSchemeIdList.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                                )
                )
                .execute();

        DeleteContextSchemeResponse response = new DeleteContextSchemeResponse(contextSchemeIdList);
        return response;
    }

}
