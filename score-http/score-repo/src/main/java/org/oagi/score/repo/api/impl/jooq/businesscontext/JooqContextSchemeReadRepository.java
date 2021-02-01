package org.oagi.score.repo.api.impl.jooq.businesscontext;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businesscontext.ContextSchemeReadRepository;
import org.oagi.score.repo.api.businesscontext.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.count;
import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.contains;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.isNull;
import static org.oagi.score.repo.api.impl.utils.StringUtils.trim;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqContextSchemeReadRepository
        extends JooqScoreRepository
        implements ContextSchemeReadRepository {

    public JooqContextSchemeReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectOnConditionStep select() {
        return dslContext().select(
                CTX_SCHEME.CTX_SCHEME_ID,
                CTX_SCHEME.GUID,
                CTX_SCHEME.SCHEME_ID,
                CTX_SCHEME.SCHEME_NAME,
                CTX_SCHEME.DESCRIPTION,
                CTX_SCHEME.SCHEME_AGENCY_ID,
                CTX_SCHEME.SCHEME_VERSION_ID,
                CTX_CATEGORY.CTX_CATEGORY_ID,
                CTX_CATEGORY.NAME,
                CODE_LIST.CODE_LIST_ID,
                CODE_LIST.NAME.as("code_list_name"),
                APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                CTX_SCHEME.CREATION_TIMESTAMP,
                CTX_SCHEME.LAST_UPDATE_TIMESTAMP)
                .from(CTX_SCHEME)
                .join(CTX_CATEGORY).on(CTX_SCHEME.CTX_CATEGORY_ID.eq(CTX_CATEGORY.CTX_CATEGORY_ID))
                .join(APP_USER.as("creator")).on(CTX_SCHEME.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(CTX_SCHEME.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .leftJoin(CODE_LIST).on(CTX_SCHEME.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID));
    }

    private RecordMapper<Record, ContextScheme> mapper() {
        return record -> {
            ContextScheme contextScheme = new ContextScheme();
            contextScheme.setContextSchemeId(record.get(CTX_SCHEME.CTX_SCHEME_ID).toBigInteger());
            contextScheme.setGuid(record.get(CTX_SCHEME.GUID));
            contextScheme.setSchemeId(record.get(CTX_SCHEME.SCHEME_ID));
            contextScheme.setSchemeName(record.get(CTX_SCHEME.SCHEME_NAME));
            contextScheme.setDescription(record.get(CTX_SCHEME.DESCRIPTION));
            contextScheme.setSchemeAgencyId(record.get(CTX_SCHEME.SCHEME_AGENCY_ID));
            contextScheme.setSchemeVersionId(record.get(CTX_SCHEME.SCHEME_VERSION_ID));
            contextScheme.setContextCategoryId(record.get(CTX_CATEGORY.CTX_CATEGORY_ID).toBigInteger());
            contextScheme.setContextCategoryName(record.get(CTX_CATEGORY.NAME));
            contextScheme.setImported(record.get(CTX_SCHEME.CODE_LIST_ID) != null);
            ULong codeListId = record.get(CODE_LIST.CODE_LIST_ID);
            if (codeListId != null) {
                contextScheme.setCodeListId(codeListId.toBigInteger());
                contextScheme.setCodeListName(record.get(CODE_LIST.NAME.as("code_list_name")));
            }
            contextScheme.setCreatedBy(new ScoreUser(
                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                    (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER
            ));
            contextScheme.setLastUpdatedBy(new ScoreUser(
                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                    (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER
            ));
            contextScheme.setCreationTimestamp(
                    Date.from(record.get(CTX_CATEGORY.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            contextScheme.setLastUpdateTimestamp(
                    Date.from(record.get(CTX_CATEGORY.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return contextScheme;
        };
    }

    private SelectConditionStep selectForValues(BigInteger contextSchemeId) {
        return dslContext().select(
                CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID,
                CTX_SCHEME_VALUE.GUID,
                CTX_SCHEME_VALUE.VALUE,
                CTX_SCHEME_VALUE.MEANING,
                CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID,
                BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID)
                .from(CTX_SCHEME_VALUE)
                .leftJoin(BIZ_CTX_VALUE)
                .on(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID.eq(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID))
                .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(ULong.valueOf(contextSchemeId)));
    }

    private RecordMapper<Record, ContextSchemeValue> mapperForValue() {
        return record -> {
            ContextSchemeValue contextSchemeValue = new ContextSchemeValue();
            contextSchemeValue.setContextSchemeValueId(
                    record.get(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID).toBigInteger());
            contextSchemeValue.setGuid(record.get(CTX_SCHEME_VALUE.GUID));
            contextSchemeValue.setValue(record.get(CTX_SCHEME_VALUE.VALUE));
            contextSchemeValue.setMeaning(record.get(CTX_SCHEME_VALUE.MEANING));
            contextSchemeValue.setUsed(record.get(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID) != null);
            contextSchemeValue.setOwnerContextSchemeId(record.get(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID).toBigInteger());
            return contextSchemeValue;
        };
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetContextSchemeResponse getContextScheme(
            GetContextSchemeRequest request) throws ScoreDataAccessException {
        ContextScheme contextScheme = null;

        BigInteger contextSchemeId = request.getContextSchemeId();
        if (!isNull(contextSchemeId)) {
            contextScheme = (ContextScheme) select()
                    .where(CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(contextSchemeId)))
                    .fetchOptional(mapper()).orElse(null);

            if (contextScheme != null) {
                contextScheme.setContextSchemeValueList(
                        selectForValues(contextSchemeId).fetch(mapperForValue())
                );

                int cnt = dslContext().select(coalesce(count(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID), 0))
                        .from(CTX_SCHEME_VALUE)
                        .join(BIZ_CTX_VALUE).on(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID.eq(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID))
                        .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(ULong.valueOf(contextSchemeId)))
                        .groupBy(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID)
                        .fetchOptionalInto(Integer.class).orElse(0);
                contextScheme.setUsed(cnt > 0);
            }
        }

        return new GetContextSchemeResponse(contextScheme);
    }

    private Collection<Condition> getConditions(GetContextSchemeListRequest request) {
        List<Condition> conditions = new ArrayList();

        if (!request.getContextSchemeIdList().isEmpty()) {
            if (request.getContextSchemeIdList().size() == 1) {
                conditions.add(CTX_SCHEME.CTX_SCHEME_ID.eq(
                        ULong.valueOf(request.getContextSchemeIdList().iterator().next())
                ));
            } else {
                conditions.add(CTX_SCHEME.CTX_SCHEME_ID.in(
                        request.getContextSchemeIdList().stream()
                                .map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
        }
        if (!request.getContextCategoryIdList().isEmpty()) {
            if (request.getContextCategoryIdList().size() == 1) {
                conditions.add(CTX_SCHEME.CTX_CATEGORY_ID.eq(
                        ULong.valueOf(request.getContextSchemeIdList().iterator().next())
                ));
            } else {
                conditions.add(CTX_SCHEME.CTX_CATEGORY_ID.in(
                        request.getContextCategoryIdList().stream()
                                .map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
        }
        if (StringUtils.hasLength(request.getSchemeName())) {
            conditions.addAll(contains(request.getSchemeName(), CTX_SCHEME.SCHEME_NAME));
        }
        if (StringUtils.hasLength(request.getDescription())) {
            conditions.addAll(contains(request.getDescription(), CTX_SCHEME.DESCRIPTION));
        }
        if (!request.getUpdaterUsernameList().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(
                    new HashSet<>(request.getUpdaterUsernameList()).stream()
                            .filter(e -> StringUtils.hasLength(e)).map(e -> trim(e)).collect(Collectors.toList())
            ));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(CTX_SCHEME.LAST_UPDATE_TIMESTAMP.greaterOrEqual(request.getUpdateStartDate()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(CTX_SCHEME.LAST_UPDATE_TIMESTAMP.lessThan(request.getUpdateEndDate()));
        }

        return conditions;
    }

    private SortField getSortField(GetContextSchemeListRequest request) {
        if (!StringUtils.hasLength(request.getSortActive())) {
            return null;
        }

        Field field;
        switch (trim(request.getSortActive()).toLowerCase()) {
            case "name":
                field = CTX_SCHEME.SCHEME_NAME;
                break;

            case "contextcategoryname":
                field = CTX_CATEGORY.NAME;
                break;

            case "description":
                field = CTX_SCHEME.DESCRIPTION;
                break;

            case "lastupdatetimestamp":
                field = CTX_SCHEME.LAST_UPDATE_TIMESTAMP;
                break;

            default:
                return null;
        }

        return (request.getSortDirection() == ASC) ? field.asc() : field.desc();
    }

    @Override
    public GetContextSchemeListResponse getContextSchemeList(
            GetContextSchemeListRequest request) throws ScoreDataAccessException {

        Collection<Condition> conditions = getConditions(request);
        SelectConditionStep conditionStep = select().where(conditions);
        SortField sortField = getSortField(request);
        int length = dslContext().fetchCount(conditionStep);
        SelectFinalStep finalStep;
        if (sortField == null) {
            if (request.isPagination()) {
                finalStep = conditionStep.limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep;
            }
        } else {
            if (request.isPagination()) {
                finalStep = conditionStep.orderBy(sortField)
                        .limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep.orderBy(sortField);
            }
        }

        List<ContextScheme> results = finalStep.fetch(mapper());
        if (!results.isEmpty()) {
            Map<BigInteger, ContextScheme> ctxSchemeMap = results.stream()
                    .collect(Collectors.toMap(ContextScheme::getContextSchemeId, Function.identity()));

            dslContext().select(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID,
                    coalesce(count(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID), 0))
                    .from(CTX_SCHEME_VALUE)
                    .join(BIZ_CTX_VALUE).on(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID.eq(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID))
                    .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.in(
                            ctxSchemeMap.keySet().stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())))
                    .groupBy(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID)
                    .fetch().stream().forEach(record -> {
                BigInteger ctxSchemeId = record.value1().toBigInteger();
                int cnt = record.value2();
                ctxSchemeMap.get(ctxSchemeId).setUsed(cnt > 0);
            });
        }

        return new GetContextSchemeListResponse(
                results,
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }

    private SortField getSortField(GetContextSchemeValueListRequest request) {
        if (!StringUtils.hasLength(request.getSortActive())) {
            return null;
        }

        Field field;
        switch (trim(request.getSortActive()).toLowerCase()) {
            case "value":
                field = CTX_SCHEME_VALUE.VALUE;
                break;

            default:
                return null;
        }

        return (request.getSortDirection() == ASC) ? field.asc() : field.desc();
    }

    @Override
    public GetContextSchemeValueListResponse getContextSchemeValueList(
            GetContextSchemeValueListRequest request) throws ScoreDataAccessException {

        List<Condition> conditions = new ArrayList();

        if (StringUtils.hasLength(request.getValue())) {
            conditions.addAll(contains(request.getValue(), CTX_SCHEME_VALUE.VALUE));
        }

        SelectConditionStep conditionStep = dslContext()
                .select(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID,
                        CTX_SCHEME_VALUE.GUID,
                        CTX_SCHEME_VALUE.VALUE,
                        CTX_SCHEME_VALUE.MEANING,
                        CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID)
                .from(CTX_SCHEME_VALUE)
                .where(conditions);
        SortField sortField = getSortField(request);
        SelectFinalStep finalStep;
        if (sortField == null) {
            if (request.isPagination()) {
                finalStep = conditionStep.limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep;
            }
        } else {
            if (request.isPagination()) {
                finalStep = conditionStep.orderBy(sortField)
                        .limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep.orderBy(sortField);
            }
        }

        return new GetContextSchemeValueListResponse(
                finalStep.fetch(record -> {
                    ContextSchemeValue contextSchemeValue = new ContextSchemeValue();
                    contextSchemeValue.setContextSchemeValueId(record.get(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID).toBigInteger());
                    contextSchemeValue.setGuid(record.get(CTX_SCHEME_VALUE.GUID));
                    contextSchemeValue.setValue(record.get(CTX_SCHEME_VALUE.VALUE));
                    contextSchemeValue.setMeaning(record.get(CTX_SCHEME_VALUE.MEANING));
                    contextSchemeValue.setOwnerContextSchemeId(record.get(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID).toBigInteger());
                    return contextSchemeValue;
                }),
                request.getPageIndex(),
                request.getPageSize(),
                dslContext().fetchCount(conditionStep)
        );
    }
}
