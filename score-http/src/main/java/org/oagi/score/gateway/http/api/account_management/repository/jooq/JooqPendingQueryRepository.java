package org.oagi.score.gateway.http.api.account_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.oagi.score.gateway.http.api.account_management.model.AppOauth2User;
import org.oagi.score.gateway.http.api.account_management.repository.PendingQueryRepository;
import org.oagi.score.gateway.http.api.account_management.repository.criteria.PendingListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.Sort;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.APP_OAUTH2_USER;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAUTH2_APP;
import static org.oagi.score.gateway.http.common.util.DSLUtils.contains;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

public class JooqPendingQueryRepository extends JooqBaseRepository implements PendingQueryRepository {

    public JooqPendingQueryRepository(DSLContext dslContext, ScoreUser requester,
                                      RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public ResultAndCount<AppOauth2User> getPendingList(PendingListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetPendingListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(filterCriteria));
        int count = dslContext().fetchCount(where);
        List<AppOauth2User> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount<>(result, count);
    }

    private class GetPendingListQueryBuilder {

        SelectOnConditionStep<? extends Record> select() {
            return dslContext().select(
                            APP_OAUTH2_USER.APP_OAUTH2_USER_ID,
                            APP_OAUTH2_USER.SUB,
                            APP_OAUTH2_USER.EMAIL,
                            APP_OAUTH2_USER.PHONE_NUMBER,
                            APP_OAUTH2_USER.NAME,
                            APP_OAUTH2_USER.NICKNAME,
                            APP_OAUTH2_USER.PREFERRED_USERNAME,
                            APP_OAUTH2_USER.CREATION_TIMESTAMP,
                            OAUTH2_APP.PROVIDER_NAME)
                    .from(APP_OAUTH2_USER)
                    .join(OAUTH2_APP).on(APP_OAUTH2_USER.OAUTH2_APP_ID.eq(OAUTH2_APP.OAUTH2_APP_ID));
        }

        List<Condition> conditions(PendingListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList<>();

            conditions.add(APP_OAUTH2_USER.APP_USER_ID.isNull());

            if (hasLength(filterCriteria.preferredUsername())) {
                conditions.addAll(contains(filterCriteria.preferredUsername(), APP_OAUTH2_USER.PREFERRED_USERNAME));
            }
            if (hasLength(filterCriteria.email())) {
                conditions.addAll(contains(filterCriteria.email(), APP_OAUTH2_USER.EMAIL));
            }
            if (hasLength(filterCriteria.providerName())) {
                conditions.addAll(contains(filterCriteria.providerName(), OAUTH2_APP.PROVIDER_NAME));
            }
            if (filterCriteria.createdTimestampRange() != null) {
                if (filterCriteria.createdTimestampRange().after() != null) {
                    conditions.add(APP_OAUTH2_USER.CREATION_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.createdTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.createdTimestampRange().before() != null) {
                    conditions.add(APP_OAUTH2_USER.CREATION_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.createdTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }

            return conditions;
        }

        List<SortField<?>> sortFields(PageRequest pageRequest) {
            List<SortField<?>> sortFields = new ArrayList<>();

            for (Sort sort : pageRequest.sorts()) {
                Field field;
                switch (sort.field()) {
                    case "creationTimestamp":
                        field = APP_OAUTH2_USER.CREATION_TIMESTAMP;
                        break;

                    case "name":
                        field = APP_OAUTH2_USER.NAME;
                        break;

                    case "nickname":
                        field = APP_OAUTH2_USER.NICKNAME;
                        break;

                    case "email":
                        field = APP_OAUTH2_USER.EMAIL;
                        break;

                    case "providerName":
                        field = OAUTH2_APP.PROVIDER_NAME;
                        break;

                    case "preferredUsername":
                        field = APP_OAUTH2_USER.PREFERRED_USERNAME;
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

        List<AppOauth2User> fetch(SelectConditionStep<?> conditionStep, PageRequest pageRequest) {
            var sortFields = sortFields(pageRequest);
            SelectFinalStep<? extends Record> finalStep;
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
            return finalStep.fetchInto(AppOauth2User.class);
        }
    }
}
