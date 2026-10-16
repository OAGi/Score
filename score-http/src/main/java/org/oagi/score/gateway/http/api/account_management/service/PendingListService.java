package org.oagi.score.gateway.http.api.account_management.service;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.repository.criteria.PendingListFilterCriteria;
import org.oagi.score.gateway.http.api.account_management.model.AppOauth2User;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AppOauth2UserRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AppUserRecord;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class PendingListService {

    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private SessionService sessionService;

    public ResultAndCount<AppOauth2User> getPendingList(ScoreUser requester,
                                                        PendingListFilterCriteria filterCriteria, PageRequest pageRequest) {
        if (!requester.isAdministrator()) {
            throw new InsufficientAuthenticationException(
                    messages.getMessage(
                            "ExceptionTranslationFilter.insufficientAuthentication",
                            "Full authentication is required to access this resource"));
        }

        return repositoryFactory.pendingQueryRepository(requester).getPendingList(filterCriteria, pageRequest);
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