package org.oagi.score.repo.api.impl.jooq.businesscontext;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businesscontext.BusinessContextReadRepository;
import org.oagi.score.repo.api.businesscontext.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.contains;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.isNull;
import static org.oagi.score.repo.api.impl.utils.StringUtils.trim;
import static org.oagi.score.repo.api.user.model.ScoreRole.*;

public class JooqBusinessContextReadRepository
        extends JooqScoreRepository
        implements BusinessContextReadRepository {

    public JooqBusinessContextReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectOnConditionStep select() {
        return dslContext().selectDistinct(
                        BIZ_CTX.BIZ_CTX_ID,
                        BIZ_CTX.GUID,
                        BIZ_CTX.NAME,
                        APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                        APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                        APP_USER.as("creator").NAME.as("creator_name"),
                        APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                        APP_USER.as("creator").IS_ADMIN.as("creator_is_admin"),
                        APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                        APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                        APP_USER.as("updater").NAME.as("updater_name"),
                        APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                        APP_USER.as("updater").IS_ADMIN.as("updater_is_admin"),
                        BIZ_CTX.CREATION_TIMESTAMP,
                        BIZ_CTX.LAST_UPDATE_TIMESTAMP)
                .from(BIZ_CTX)
                .join(APP_USER.as("creator")).on(BIZ_CTX.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(BIZ_CTX.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .leftJoin(TENANT_BUSINESS_CTX).on(BIZ_CTX.BIZ_CTX_ID.eq(TENANT_BUSINESS_CTX.BIZ_CTX_ID));
    }

    private RecordMapper<Record, BusinessContext> mapper() {
        return record -> {
            BusinessContext businessContext = new BusinessContext();
            businessContext.setBusinessContextId(record.get(BIZ_CTX.BIZ_CTX_ID).toBigInteger());
            businessContext.setGuid(record.get(BIZ_CTX.GUID));
            businessContext.setName(record.get(BIZ_CTX.NAME));
            businessContext.setUsed(dslContext().selectCount().from(BIZ_CTX_ASSIGNMENT)
                    .where(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(record.get(BIZ_CTX.BIZ_CTX_ID)))
                    .fetchOneInto(Integer.class) > 0);

            ScoreRole creatorRole = (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER;
            boolean isCreatorAdmin = (byte) 1 == record.get(APP_USER.as("creator").IS_ADMIN.as("creator_is_admin"));
            businessContext.setCreatedBy(
                    (isCreatorAdmin) ?
                            new ScoreUser(
                                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                                    record.get(APP_USER.as("creator").NAME.as("creator_name")),
                                    Arrays.asList(creatorRole, ADMINISTRATOR)) :
                            new ScoreUser(
                                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                                    record.get(APP_USER.as("creator").NAME.as("creator_name")),
                                    creatorRole));

            ScoreRole updaterRole = (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER;
            boolean isUpdaterAdmin = (byte) 1 == record.get(APP_USER.as("updater").IS_ADMIN.as("updater_is_admin"));
            businessContext.setLastUpdatedBy(
                    (isUpdaterAdmin) ?
                            new ScoreUser(
                                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                                    record.get(APP_USER.as("updater").NAME.as("updater_name")),
                                    Arrays.asList(updaterRole, ADMINISTRATOR)) :
                            new ScoreUser(
                                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                                    record.get(APP_USER.as("updater").NAME.as("updater_name")),
                                    updaterRole));

            businessContext.setCreationTimestamp(
                    Date.from(record.get(BIZ_CTX.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            businessContext.setLastUpdateTimestamp(
                    Date.from(record.get(BIZ_CTX.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return businessContext;
        };
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetBusinessContextResponse getBusinessContext(
            GetBusinessContextRequest request) throws ScoreDataAccessException {
        BusinessContext businessContext = null;

        BigInteger businessContextId = request.getBusinessContextId();
        if (!isNull(businessContextId)) {
            businessContext = (BusinessContext) select()
                    .where(BIZ_CTX.BIZ_CTX_ID.eq(ULong.valueOf(businessContextId)))
                    .fetchOne(mapper());

            if (businessContext != null) {
                businessContext.setBusinessContextValueList(
                        selectForValues(businessContextId).fetch(mapperForValue())
                );
            }
        }

        return new GetBusinessContextResponse(businessContext);
    }

    private SelectConditionStep selectForValues(BigInteger businessContextId) {
        return dslContext().select(
                        BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID,
                        BIZ_CTX_VALUE.BIZ_CTX_ID,
                        CTX_CATEGORY.CTX_CATEGORY_ID,
                        CTX_CATEGORY.NAME.as("ctx_category_name"),
                        CTX_SCHEME.CTX_SCHEME_ID,
                        CTX_SCHEME.SCHEME_NAME.as("ctx_scheme_name"),
                        CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID,
                        CTX_SCHEME_VALUE.VALUE.as("ctx_scheme_value"),
                        CTX_SCHEME_VALUE.MEANING.as("ctx_scheme_value_meaning"))
                .from(BIZ_CTX_VALUE)
                .join(CTX_SCHEME_VALUE).on(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID.equal(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID))
                .join(CTX_SCHEME).on(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.equal(CTX_SCHEME.CTX_SCHEME_ID))
                .join(CTX_CATEGORY).on(CTX_SCHEME.CTX_CATEGORY_ID.equal(CTX_CATEGORY.CTX_CATEGORY_ID))
                .where(BIZ_CTX_VALUE.BIZ_CTX_ID.eq(ULong.valueOf(businessContextId)));
    }

    private RecordMapper<Record, BusinessContextValue> mapperForValue() {
        return record -> {
            BusinessContextValue businessContextValue = new BusinessContextValue();
            businessContextValue.setBusinessContextValueId(record.get(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID).toBigInteger());
            businessContextValue.setBusinessContextId(record.get(BIZ_CTX_VALUE.BIZ_CTX_ID).toBigInteger());

            businessContextValue.setContextCategoryId(record.get(CTX_CATEGORY.CTX_CATEGORY_ID).toBigInteger());
            businessContextValue.setContextCategoryName(record.get(CTX_CATEGORY.NAME.as("ctx_category_name")));

            businessContextValue.setContextSchemeId(record.get(CTX_SCHEME.CTX_SCHEME_ID).toBigInteger());
            businessContextValue.setContextSchemeName(record.get(CTX_SCHEME.SCHEME_NAME.as("ctx_scheme_name")));

            businessContextValue.setContextSchemeValueId(record.get(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID).toBigInteger());
            businessContextValue.setContextSchemeValue(record.get(CTX_SCHEME_VALUE.VALUE.as("ctx_scheme_value")));
            businessContextValue.setContextSchemeValueMeaning(record.get(CTX_SCHEME_VALUE.MEANING.as("ctx_scheme_value_meaning")));

            return businessContextValue;
        };
    }

    private Collection<Condition> getConditions(GetBusinessContextListRequest request, boolean isTenantInstance) {
        List<Condition> conditions = new ArrayList();

        if (!request.getBusinessContextIdList().isEmpty()) {
            if (request.getBusinessContextIdList().size() == 1) {
                conditions.add(BIZ_CTX.BIZ_CTX_ID.eq(
                        ULong.valueOf(request.getBusinessContextIdList().iterator().next())
                ));
            } else {
                conditions.add(BIZ_CTX.BIZ_CTX_ID.in(
                        request.getBusinessContextIdList().stream()
                                .map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
        }
        if (!request.getTopLevelAsbiepIdList().isEmpty()) {
            if (request.getTopLevelAsbiepIdList().size() == 1) {
                conditions.add(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(
                        ULong.valueOf(request.getTopLevelAsbiepIdList().iterator().next())
                ));
            } else {
                conditions.add(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.in(
                        request.getTopLevelAsbiepIdList().stream()
                                .map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
        }
        if (StringUtils.hasLength(request.getName())) {
            conditions.addAll(contains(request.getName(), BIZ_CTX.NAME));
        }
        if (!request.getUpdaterUsernameList().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(
                    new HashSet<>(request.getUpdaterUsernameList()).stream()
                            .filter(e -> StringUtils.hasLength(e)).map(e -> trim(e)).collect(Collectors.toList())
            ));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(BIZ_CTX.LAST_UPDATE_TIMESTAMP.greaterOrEqual(request.getUpdateStartDate()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(BIZ_CTX.LAST_UPDATE_TIMESTAMP.lessThan(request.getUpdateEndDate()));
        }

        // for tenant management
        if (isTenantInstance) {
            Long tenantId = request.getTenantId();
            boolean notConnectedToTenant = request.isNotConnectedToTenant();
            if (tenantId != null && !notConnectedToTenant) {
                conditions.add(TENANT_BUSINESS_CTX.TENANT_ID.eq(ULong.valueOf(tenantId)));
            }

            if (tenantId != null && notConnectedToTenant) {
                conditions.add(BIZ_CTX.BIZ_CTX_ID.notIn(dslContext().select(TENANT_BUSINESS_CTX.BIZ_CTX_ID)
                        .from(TENANT_BUSINESS_CTX).where(TENANT_BUSINESS_CTX.TENANT_ID.eq(ULong.valueOf(tenantId)))));
            }
        }

        // for editing bie
        if (isTenantInstance && request.isBieEditing()) {
            conditions.add(BIZ_CTX.BIZ_CTX_ID.in(dslContext().select(TENANT_BUSINESS_CTX.BIZ_CTX_ID)
                    .from(TENANT_BUSINESS_CTX).where(TENANT_BUSINESS_CTX.TENANT_ID.in(request.getUserTenantIds()))));
        }

        return conditions;
    }

    private SortField getSortField(GetBusinessContextListRequest request) {
        if (!StringUtils.hasLength(request.getSortActive())) {
            return null;
        }

        Field field;
        switch (trim(request.getSortActive()).toLowerCase()) {
            case "name":
                field = BIZ_CTX.NAME;
                break;

            case "lastupdatetimestamp":
                field = BIZ_CTX.LAST_UPDATE_TIMESTAMP;
                break;

            default:
                return null;
        }

        return (request.getSortDirection() == ASC) ? field.asc() : field.desc();
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetBusinessContextListResponse getBusinessContextList(
            GetBusinessContextListRequest request, boolean isTenantInstance) throws ScoreDataAccessException {
        Collection<Condition> conditions = getConditions(request, isTenantInstance);

        SelectConditionStep conditionStep;
        if (!request.getTopLevelAsbiepIdList().isEmpty()) {
            conditionStep = select()
                    .join(BIZ_CTX_ASSIGNMENT).on(BIZ_CTX.BIZ_CTX_ID.eq(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID))
                    .where(conditions);
        } else {
            conditionStep = select().where(conditions);
        }
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

        return new GetBusinessContextListResponse(
                finalStep.fetch(mapper()),
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }
}
