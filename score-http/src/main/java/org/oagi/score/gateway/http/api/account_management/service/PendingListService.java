package org.oagi.score.gateway.http.api.account_management.service;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.controller.payload.PendingListRequest;
import org.oagi.score.gateway.http.api.account_management.model.AppOauth2User;
import org.oagi.score.gateway.http.common.filter.ContainsFilterBuilder;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.Sort;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AppOauth2UserRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AppUserRecord;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class PendingListService {

    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    public PageResponse<AppOauth2User> getPendingList(ScoreUser requester, PendingListRequest request) {
        if (!requester.isAdministrator()) {
            throw new InsufficientAuthenticationException(
                    messages.getMessage(
                            "ExceptionTranslationFilter.insufficientAuthentication",
                            "Full authentication is required to access this resource"));
        }

        SelectOnConditionStep step = dslContext.select(
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

        List<Condition> conditions = new ArrayList();
        conditions.add(APP_OAUTH2_USER.APP_USER_ID.isNull());

        if (StringUtils.hasLength(request.getPreferredUsername())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getPreferredUsername(), APP_OAUTH2_USER.PREFERRED_USERNAME));
        }
        if (StringUtils.hasLength(request.getEmail())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getEmail(), APP_OAUTH2_USER.EMAIL));
        }
        if (StringUtils.hasLength(request.getProviderName())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getProviderName(), OAUTH2_APP.PROVIDER_NAME));
        }
        if (request.getCreateStartDate() != null) {
            conditions.add(APP_OAUTH2_USER.CREATION_TIMESTAMP.greaterOrEqual((Field<LocalDateTime>) request.getCreateStartDate()));
        }
        if (request.getCreateEndDate() != null) {
            conditions.add(APP_OAUTH2_USER.CREATION_TIMESTAMP.lessThan((Field<LocalDateTime>) request.getCreateEndDate()));
        }

        SelectConditionStep<Record11<ULong, String, String, String,
                String, String, String, String,
                Timestamp, Byte, String>> conditionStep = step.where(and(conditions));
        int pageCount = dslContext.fetchCount(conditionStep);

        PageRequest pageRequest = request.getPageRequest();
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

        List<AppOauth2User> result = finalStep.fetchInto(AppOauth2User.class);

        PageResponse<AppOauth2User> response = new PageResponse();
        response.setList(result);
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(pageCount);
        return response;
    }

    public List<SortField<?>> sortFields(PageRequest pageRequest) {
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

    public AppOauth2User getPending(long appOauth2UserId) {
        return dslContext.select(
                APP_OAUTH2_USER.APP_OAUTH2_USER_ID,
                APP_OAUTH2_USER.APP_USER_ID,
                APP_OAUTH2_USER.SUB,
                APP_OAUTH2_USER.EMAIL,
                APP_OAUTH2_USER.PHONE_NUMBER,
                APP_OAUTH2_USER.NAME,
                APP_OAUTH2_USER.NICKNAME,
                APP_OAUTH2_USER.PREFERRED_USERNAME,
                APP_OAUTH2_USER.CREATION_TIMESTAMP,
                OAUTH2_APP.PROVIDER_NAME)
                .from(APP_OAUTH2_USER)
                .join(OAUTH2_APP).on(APP_OAUTH2_USER.OAUTH2_APP_ID.eq(OAUTH2_APP.OAUTH2_APP_ID))
                .where((APP_OAUTH2_USER.APP_OAUTH2_USER_ID.eq(ULong.valueOf(appOauth2UserId))))
                .fetchOneInto(AppOauth2User.class);
    }

    public AppOauth2User getPendingBySub(String sub) {
        return dslContext.select(
                APP_OAUTH2_USER.APP_OAUTH2_USER_ID,
                APP_OAUTH2_USER.APP_USER_ID,
                APP_OAUTH2_USER.SUB,
                APP_OAUTH2_USER.EMAIL,
                APP_OAUTH2_USER.PHONE_NUMBER,
                APP_OAUTH2_USER.NAME,
                APP_OAUTH2_USER.NICKNAME,
                APP_OAUTH2_USER.PREFERRED_USERNAME,
                APP_OAUTH2_USER.CREATION_TIMESTAMP,
                OAUTH2_APP.PROVIDER_NAME)
                .from(APP_OAUTH2_USER)
                .join(OAUTH2_APP).on(APP_OAUTH2_USER.OAUTH2_APP_ID.eq(OAUTH2_APP.OAUTH2_APP_ID))
                .where((APP_OAUTH2_USER.SUB.eq(sub)))
                .fetchOneInto(AppOauth2User.class);
    }

    @Transactional
    public void deletePending(AppOauth2User appOauth2User) {
        if (appOauth2User.getAppOauth2UserId() > 0) {
            dslContext.deleteFrom(APP_OAUTH2_USER)
                    .where(APP_OAUTH2_USER.APP_OAUTH2_USER_ID.eq(ULong.valueOf(appOauth2User.getAppOauth2UserId())))
                    .execute();
        }
    }

    @Transactional
    public void linkPendingToAppUser(long appOauth2UserId, long appUserId) {
        AppUserRecord appUserRecord = dslContext.selectFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(appUserId))).fetchOne();

        if (appUserRecord == null) {
            throw new IllegalArgumentException("Cannot find target Account");
        }

        AppOauth2UserRecord appOauth2UserRecord = dslContext.selectFrom(APP_OAUTH2_USER)
                .where(APP_OAUTH2_USER.APP_OAUTH2_USER_ID.eq(ULong.valueOf(appOauth2UserId))).fetchOne();

        if (appOauth2UserRecord == null) {
            throw new IllegalArgumentException("Cannot find target Pending Account");
        }

        dslContext.update(APP_OAUTH2_USER)
                .setNull(APP_OAUTH2_USER.APP_USER_ID)
                .where(APP_OAUTH2_USER.APP_USER_ID.eq(appUserRecord.getAppUserId()))
                .execute();

        dslContext.update(APP_OAUTH2_USER)
                .set(APP_OAUTH2_USER.APP_USER_ID, appUserRecord.getAppUserId())
                .where(APP_OAUTH2_USER.APP_OAUTH2_USER_ID.eq(appOauth2UserRecord.getAppOauth2UserId()))
                .execute();
    }
}