package org.oagi.score.gateway.http.api.account_management.service;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.gateway.http.api.account_management.data.AppOauth2User;
import org.oagi.score.gateway.http.api.account_management.data.PendingListRequest;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.filter.ContainsFilterBuilder;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AppOauth2UserRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AppUserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class PendingListService {

    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    public PageResponse<AppOauth2User> getPendingList(AuthenticatedPrincipal user, PendingListRequest request) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        if (!requester.isAdmin()) {
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

        PageRequest pageRequest = request.getPageRequest();
        String sortDirection = pageRequest.getSortDirection();
        SortField sortField = null;
        switch (pageRequest.getSortActive()) {
            case "creationTimestamp":
                if ("asc".equals(sortDirection)) {
                    sortField = APP_OAUTH2_USER.CREATION_TIMESTAMP.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = APP_OAUTH2_USER.CREATION_TIMESTAMP.desc();
                }

                break;
            case "name":
                if ("asc".equals(sortDirection)) {
                    sortField = APP_OAUTH2_USER.NAME.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = APP_OAUTH2_USER.NAME.desc();
                }

                break;
            case "nickname":
                if ("asc".equals(sortDirection)) {
                    sortField = APP_OAUTH2_USER.NICKNAME.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = APP_OAUTH2_USER.NICKNAME.desc();
                }

                break;
            case "providerName":
                if ("asc".equals(sortDirection)) {
                    sortField = OAUTH2_APP.PROVIDER_NAME.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = OAUTH2_APP.PROVIDER_NAME.desc();
                }

                break;

            case "preferredUsername":
                if ("asc".equals(sortDirection)) {
                    sortField = APP_OAUTH2_USER.PREFERRED_USERNAME.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = APP_OAUTH2_USER.PREFERRED_USERNAME.desc();
                }

                break;
        }
        int pageCount = dslContext.fetchCount(conditionStep);
        SelectWithTiesAfterOffsetStep<Record11<ULong, String, String, String,
                String, String, String, String,
                Timestamp, Byte, String>> offsetStep = null;
        if (sortField != null) {
            offsetStep = conditionStep.orderBy(sortField)
                    .limit(pageRequest.getOffset(), pageRequest.getPageSize());
        } else {
            if (pageRequest.getPageIndex() >= 0 && pageRequest.getPageSize() > 0) {
                offsetStep = conditionStep
                        .limit(pageRequest.getOffset(), pageRequest.getPageSize());
            }
        }

        List<AppOauth2User> result = (offsetStep != null) ?
                offsetStep.fetchInto(AppOauth2User.class) : conditionStep.fetchInto(AppOauth2User.class);

        PageResponse<AppOauth2User> response = new PageResponse();
        response.setList(result);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(pageCount);
        return response;
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