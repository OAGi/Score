package org.oagi.score.gateway.http.api.account_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.oagi.score.gateway.http.api.account_management.model.*;
import org.oagi.score.gateway.http.api.account_management.repository.AccountQueryRepository;
import org.oagi.score.gateway.http.api.account_management.repository.criteria.AccountListFilterCriteria;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.Sort;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.or;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.springframework.util.StringUtils.hasLength;

public class JooqAccountQueryRepository extends JooqBaseRepository implements AccountQueryRepository {

    public JooqAccountQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public ResultAndCount<AccountListEntryRecord> getAccountList(
            AccountListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetAccountListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(filterCriteria));
        int count = dslContext().fetchCount(where);
        List<AccountListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetAccountListQueryBuilder {

        SelectOnConditionStep<? extends Record> select() {
            return dslContext().selectDistinct(
                            APP_USER.APP_USER_ID,
                            APP_USER.LOGIN_ID,
                            APP_USER.NAME,
                            APP_USER.ORGANIZATION,
                            APP_USER.IS_DEVELOPER.as("developer"),
                            APP_USER.IS_ADMIN.as("admin"),
                            APP_USER.IS_ENABLED.as("enabled"),
                            APP_OAUTH2_USER.APP_OAUTH2_USER_ID
                    ).from(APP_USER)
                    .leftJoin(APP_OAUTH2_USER).on(APP_USER.APP_USER_ID.eq(APP_OAUTH2_USER.APP_USER_ID))
                    .leftJoin(USER_TENANT)
                    .on(USER_TENANT.APP_USER_ID.eq(APP_USER.APP_USER_ID));
        }

        List<Condition> conditions(AccountListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();

            if (hasLength(filterCriteria.loginId())) {
                conditions.add(APP_USER.LOGIN_ID.containsIgnoreCase(filterCriteria.loginId().trim()));
            }
            if (hasLength(filterCriteria.name())) {
                conditions.add(APP_USER.NAME.containsIgnoreCase(filterCriteria.name().trim()));
            }
            if (hasLength(filterCriteria.organization())) {
                conditions.add(APP_USER.ORGANIZATION.containsIgnoreCase(filterCriteria.organization().trim()));
            }
            if (filterCriteria.enabled() != null) {
                conditions.add(APP_USER.IS_ENABLED.eq((byte) (filterCriteria.enabled() ? 1 : 0)));
            }
            List<Condition> roleConditions = new ArrayList();
            for (String role : filterCriteria.roles()) {
                switch (role) {
                    case "developer":
                        roleConditions.add(APP_USER.IS_DEVELOPER.eq((byte) 1));
                        break;
                    case "end-user":
                        roleConditions.add(APP_USER.IS_DEVELOPER.eq((byte) 0));
                        break;
                    case "admin":
                        roleConditions.add(APP_USER.IS_ADMIN.eq((byte) 1));
                        break;
                }
            }
            if (!roleConditions.isEmpty()) {
                if (roleConditions.size() == 1) {
                    conditions.add(roleConditions.get(0));
                } else {
                    conditions.add(or(roleConditions));
                }
            }
            if (filterCriteria.excludeSSO()) {
                conditions.add(APP_OAUTH2_USER.APP_OAUTH2_USER_ID.isNull());
            }
            Boolean excludeRequester = filterCriteria.excludeRequester();
            if (excludeRequester != null && excludeRequester == true) {
                conditions.add(APP_USER.LOGIN_ID.notEqualIgnoreCase(requester().username().trim()));
            }

            boolean tenantEnabled = filterCriteria.tenantEnabled();
            if (tenantEnabled) {
                additionalConditionsForTenant(conditions,
                        filterCriteria.tenantId(), filterCriteria.notConnectedToTenant(),
                        filterCriteria.businessContextIdList());
            }

            return conditions;
        }

        private void additionalConditionsForTenant(List<Condition> conditions,
                                                   TenantId tenantId, boolean notConnectedToTenant,
                                                   Collection<BusinessContextId> businessContextIdList) {
            if (tenantId != null && !notConnectedToTenant) {
                conditions.add(USER_TENANT.TENANT_ID.eq(valueOf(tenantId)));
            }

            if (tenantId != null && notConnectedToTenant) {
                conditions.add(APP_USER.APP_USER_ID.notIn(
                        dslContext().select(USER_TENANT.APP_USER_ID)
                                .from(USER_TENANT)
                                .where(USER_TENANT.TENANT_ID.eq(valueOf(tenantId)))));
            }

            if (businessContextIdList != null && !businessContextIdList.isEmpty()) {
                conditions.add(USER_TENANT.TENANT_ID.in(
                        dslContext().select(TENANT_BUSINESS_CTX.TENANT_ID)
                                .from(TENANT_BUSINESS_CTX)
                                .where(TENANT_BUSINESS_CTX.BIZ_CTX_ID.in(valueOf(businessContextIdList)))));
            }
        }

        public List<SortField<?>> sortFields(PageRequest pageRequest) {
            List<SortField<?>> sortFields = new ArrayList<>();

            for (Sort sort : pageRequest.sorts()) {
                Field field;
                switch (sort.field()) {
                    case "loginId":
                        field = APP_USER.LOGIN_ID;
                        break;

                    case "role":
                        field = APP_USER.IS_DEVELOPER;
                        break;

                    case "name":
                        field = APP_USER.NAME;
                        break;

                    case "organization":
                        field = APP_USER.ORGANIZATION;
                        break;

                    case "status":
                        field = APP_USER.IS_ENABLED;
                        break;

                    default:
                        continue;
                }

                if (sort.direction() == DESC) {
                    sortFields.add(field.desc());
                } else {
                    sortFields.add(field.asc());
                }
            }

            return sortFields;
        }

        List<AccountListEntryRecord> fetch(SelectConditionStep<?> conditionStep, PageRequest pageRequest) {
            var sortFields = sortFields(pageRequest);
            SelectFinalStep<? extends org.jooq.Record> finalStep;
            if (sortFields == null || sortFields.isEmpty()) {
                if (pageRequest.isPagination()) {
                    finalStep = conditionStep.limit(pageRequest.pageOffset(), pageRequest.pageSize());
                } else {
                    finalStep = conditionStep;
                }
            } else {
                if (pageRequest.isPagination()) {
                    finalStep = conditionStep.orderBy(sortFields)
                            .limit(pageRequest.pageOffset(), pageRequest.pageSize());
                } else {
                    finalStep = conditionStep.orderBy(sortFields);
                }
            }
            return finalStep.fetch(mapper());
        }

        RecordMapper<org.jooq.Record, AccountListEntryRecord> mapper() {
            return record -> {
                UserId userId = new UserId(record.get(APP_USER.APP_USER_ID).toBigInteger());
                return new AccountListEntryRecord(userId,
                        record.get(APP_USER.LOGIN_ID),
                        record.get(APP_USER.NAME),
                        record.get(APP_USER.ORGANIZATION),
                        record.get(APP_USER.IS_DEVELOPER.as("developer")) == (byte) 1,
                        record.get(APP_USER.IS_ADMIN.as("admin")) == (byte) 1,
                        record.get(APP_USER.IS_ENABLED.as("enabled")) == (byte) 1,
                        record.get(APP_OAUTH2_USER.APP_OAUTH2_USER_ID) != null ?
                                new OAuth2UserId(record.get(APP_OAUTH2_USER.APP_OAUTH2_USER_ID).toBigInteger()) : null);
            };
        }

    }

    @Override
    public AccountDetailsRecord getAccountDetails(UserId appUserId) {
        if (appUserId == null) {
            return null;
        }

        var queryBuilder = new GetAccountDetailsQueryBuilder();
        return queryBuilder.select()
                .where(APP_USER.APP_USER_ID.eq(valueOf(appUserId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public AccountDetailsRecord getAccountDetailsByLoginId(String username) {
        if (!hasLength(username)) {
            return null;
        }

        var queryBuilder = new GetAccountDetailsQueryBuilder();
        return queryBuilder.select()
                .where(APP_USER.LOGIN_ID.eq(username))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetAccountDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(
                            APP_USER.APP_USER_ID,
                            APP_USER.LOGIN_ID,
                            APP_USER.NAME,
                            APP_USER.IS_DEVELOPER.as("developer"),
                            APP_USER.IS_ADMIN.as("admin"),
                            APP_USER.IS_ENABLED.as("enabled"),
                            APP_USER.ORGANIZATION,
                            APP_USER.EMAIL,
                            APP_USER.EMAIL_VERIFIED,
                            APP_OAUTH2_USER.APP_OAUTH2_USER_ID,
                            OAUTH2_APP.PROVIDER_NAME,
                            APP_OAUTH2_USER.SUB,
                            APP_OAUTH2_USER.NAME.as("oidc_name"),
                            APP_OAUTH2_USER.EMAIL.as("oidc_email"),
                            APP_OAUTH2_USER.NICKNAME,
                            APP_OAUTH2_USER.PREFERRED_USERNAME,
                            APP_OAUTH2_USER.PHONE_NUMBER)
                    .from(APP_USER)
                    .leftJoin(APP_OAUTH2_USER)
                    .on(APP_USER.APP_USER_ID.eq(APP_OAUTH2_USER.APP_USER_ID))
                    .leftJoin(OAUTH2_APP)
                    .on(APP_OAUTH2_USER.OAUTH2_APP_ID.eq(OAUTH2_APP.OAUTH2_APP_ID));
        }

        RecordMapper<org.jooq.Record, AccountDetailsRecord> mapper() {
            return record -> {
                UserId userId = new UserId(record.get(APP_USER.APP_USER_ID).toBigInteger());
                return new AccountDetailsRecord(userId,
                        record.get(APP_USER.LOGIN_ID),
                        record.get(APP_USER.NAME),
                        record.get(APP_USER.ORGANIZATION),
                        record.get(APP_USER.EMAIL),
                        record.get(APP_USER.IS_DEVELOPER.as("developer")) == (byte) 1,
                        record.get(APP_USER.IS_ADMIN.as("admin")) == (byte) 1,
                        record.get(APP_USER.IS_ENABLED.as("enabled")) == (byte) 1,
                        record.get(APP_USER.EMAIL_VERIFIED) == (byte) 1,
                        hasData(userId),
                        record.get(APP_OAUTH2_USER.APP_OAUTH2_USER_ID) != null ?
                                new OAuth2UserId(record.get(APP_OAUTH2_USER.APP_OAUTH2_USER_ID).toBigInteger()) : null,
                        record.get(OAUTH2_APP.PROVIDER_NAME),
                        record.get(APP_OAUTH2_USER.SUB),
                        record.get(APP_OAUTH2_USER.NAME.as("oidc_name")),
                        record.get(APP_OAUTH2_USER.EMAIL.as("oidc_email")),
                        record.get(APP_OAUTH2_USER.NICKNAME),
                        record.get(APP_OAUTH2_USER.PREFERRED_USERNAME),
                        record.get(APP_OAUTH2_USER.PHONE_NUMBER));
            };
        }
    }

    private boolean hasData(UserId appUserId) {
        return (dslContext().selectCount()
                .from(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(valueOf(appUserId)))
                .fetchOptionalInto(Integer.class).orElse(0) +
                dslContext().selectCount()
                        .from(ACC)
                        .where(or(
                                ACC.OWNER_USER_ID.eq(valueOf(appUserId)),
                                ACC.CREATED_BY.eq(valueOf(appUserId)),
                                ACC.LAST_UPDATED_BY.eq(valueOf(appUserId))
                        ))
                        .fetchOptionalInto(Integer.class).orElse(0) +
                dslContext().selectCount()
                        .from(ASCC)
                        .where(or(
                                ASCC.OWNER_USER_ID.eq(valueOf(appUserId)),
                                ASCC.CREATED_BY.eq(valueOf(appUserId)),
                                ASCC.LAST_UPDATED_BY.eq(valueOf(appUserId))
                        ))
                        .fetchOptionalInto(Integer.class).orElse(0) +
                dslContext().selectCount()
                        .from(BCC)
                        .where(or(
                                BCC.OWNER_USER_ID.eq(valueOf(appUserId)),
                                BCC.CREATED_BY.eq(valueOf(appUserId)),
                                BCC.LAST_UPDATED_BY.eq(valueOf(appUserId))
                        ))
                        .fetchOptionalInto(Integer.class).orElse(0) +
                dslContext().selectCount()
                        .from(ASCCP)
                        .where(or(
                                ASCCP.OWNER_USER_ID.eq(valueOf(appUserId)),
                                ASCCP.CREATED_BY.eq(valueOf(appUserId)),
                                ASCCP.LAST_UPDATED_BY.eq(valueOf(appUserId))
                        ))
                        .fetchOptionalInto(Integer.class).orElse(0) +
                dslContext().selectCount()
                        .from(BCCP)
                        .where(or(
                                BCCP.OWNER_USER_ID.eq(valueOf(appUserId)),
                                BCCP.CREATED_BY.eq(valueOf(appUserId)),
                                BCCP.LAST_UPDATED_BY.eq(valueOf(appUserId))
                        ))
                        .fetchOptionalInto(Integer.class).orElse(0) +
                dslContext().selectCount()
                        .from(DT)
                        .where(or(
                                DT.OWNER_USER_ID.eq(valueOf(appUserId)),
                                DT.CREATED_BY.eq(valueOf(appUserId)),
                                DT.LAST_UPDATED_BY.eq(valueOf(appUserId))
                        ))
                        .fetchOptionalInto(Integer.class).orElse(0) +
                dslContext().selectCount()
                        .from(CODE_LIST)
                        .where(or(
                                CODE_LIST.OWNER_USER_ID.eq(valueOf(appUserId)),
                                CODE_LIST.CREATED_BY.eq(valueOf(appUserId)),
                                CODE_LIST.LAST_UPDATED_BY.eq(valueOf(appUserId))
                        ))
                        .fetchOptionalInto(Integer.class).orElse(0) +
                dslContext().selectCount()
                        .from(AGENCY_ID_LIST)
                        .where(or(
                                AGENCY_ID_LIST.OWNER_USER_ID.eq(valueOf(appUserId)),
                                AGENCY_ID_LIST.CREATED_BY.eq(valueOf(appUserId)),
                                AGENCY_ID_LIST.LAST_UPDATED_BY.eq(valueOf(appUserId))
                        ))
                        .fetchOptionalInto(Integer.class).orElse(0) +
                dslContext().selectCount()
                        .from(CTX_CATEGORY)
                        .where(or(
                                CTX_CATEGORY.CREATED_BY.eq(valueOf(appUserId)),
                                CTX_CATEGORY.LAST_UPDATED_BY.eq(valueOf(appUserId))
                        ))
                        .fetchOptionalInto(Integer.class).orElse(0) +
                dslContext().selectCount()
                        .from(CTX_SCHEME)
                        .where(or(
                                CTX_SCHEME.CREATED_BY.eq(valueOf(appUserId)),
                                CTX_SCHEME.LAST_UPDATED_BY.eq(valueOf(appUserId))
                        ))
                        .fetchOptionalInto(Integer.class).orElse(0) +
                dslContext().selectCount()
                        .from(BIZ_CTX)
                        .where(or(
                                BIZ_CTX.CREATED_BY.eq(valueOf(appUserId)),
                                BIZ_CTX.LAST_UPDATED_BY.eq(valueOf(appUserId))
                        ))
                        .fetchOptionalInto(Integer.class).orElse(0)
        ) > 0;
    }

    @Override
    public String getEncodedPassword(UserId userId) {
        if (userId == null) {
            return null;
        }
        return dslContext().select(APP_USER.PASSWORD)
                .from(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(valueOf(userId)))
                .fetchOneInto(String.class);
    }

    @Override
    public List<String> getLoginIdList() {
        return dslContext().select(APP_USER.LOGIN_ID)
                .from(APP_USER)
                .fetchStreamInto(String.class)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    @Override
    public OAuth2UserRecord getOAuth2User(OAuth2UserId oAuth2UserId, String sub) {
        if (oAuth2UserId == null || !hasLength(sub)) {
            return null;
        }

        var queryBuilder = new GetOAuth2UserQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        APP_OAUTH2_USER.APP_OAUTH2_USER_ID.eq(valueOf(oAuth2UserId)),
                        APP_OAUTH2_USER.SUB.eq(sub)
                ))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public OAuth2UserRecord getOAuth2User(String sub) {
        if (!hasLength(sub)) {
            return null;
        }

        var queryBuilder = new GetOAuth2UserQueryBuilder();
        return queryBuilder.select()
                .where(APP_OAUTH2_USER.SUB.eq(sub))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public OAuth2UserRecord getOAuth2User(UserId userId) {
        if (userId == null) {
            return null;
        }

        var queryBuilder = new GetOAuth2UserQueryBuilder();
        return queryBuilder.select()
                .where(APP_OAUTH2_USER.APP_USER_ID.eq(valueOf(userId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetOAuth2UserQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(APP_OAUTH2_USER.APP_OAUTH2_USER_ID,
                            APP_USER.APP_USER_ID,
                            OAUTH2_APP.PROVIDER_NAME,
                            APP_OAUTH2_USER.SUB,
                            APP_OAUTH2_USER.NAME,
                            APP_OAUTH2_USER.EMAIL,
                            APP_OAUTH2_USER.NICKNAME,
                            APP_OAUTH2_USER.PREFERRED_USERNAME,
                            APP_OAUTH2_USER.PHONE_NUMBER,
                            APP_OAUTH2_USER.CREATION_TIMESTAMP)
                    .from(APP_OAUTH2_USER)
                    .join(OAUTH2_APP).on(APP_OAUTH2_USER.OAUTH2_APP_ID.eq(OAUTH2_APP.OAUTH2_APP_ID))
                    .leftJoin(APP_USER).on(APP_OAUTH2_USER.APP_USER_ID.eq(APP_USER.APP_USER_ID));
        }

        RecordMapper<org.jooq.Record, OAuth2UserRecord> mapper() {
            return record -> {
                return new OAuth2UserRecord(
                        new OAuth2UserId(record.get(APP_OAUTH2_USER.APP_OAUTH2_USER_ID).toBigInteger()),
                        (record.get(APP_USER.APP_USER_ID) != null) ?
                                new UserId(record.get(APP_USER.APP_USER_ID).toBigInteger()) : null,
                        record.get(OAUTH2_APP.PROVIDER_NAME),
                        record.get(APP_OAUTH2_USER.SUB),
                        record.get(APP_OAUTH2_USER.NAME),
                        record.get(APP_OAUTH2_USER.EMAIL),
                        record.get(APP_OAUTH2_USER.NICKNAME),
                        record.get(APP_OAUTH2_USER.PREFERRED_USERNAME),
                        record.get(APP_OAUTH2_USER.PHONE_NUMBER),
                        toDate(record.get(APP_OAUTH2_USER.CREATION_TIMESTAMP))
                );
            };
        }
    }

}
