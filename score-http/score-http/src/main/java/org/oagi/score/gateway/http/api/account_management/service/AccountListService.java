package org.oagi.score.gateway.http.api.account_management.service;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.account_management.data.AccountListRequest;
import org.oagi.score.gateway.http.api.account_management.data.AppUser;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.tenant_management.service.TenantService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AppOauth2UserRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AppUserRecord;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.or;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class AccountListService {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ApplicationConfigurationService configService;

    @Autowired
    private TenantService tenantService;

    public PageResponse<AppUser> getAccounts(AuthenticatedPrincipal user,
                                             AccountListRequest request) {
        SelectOnConditionStep step = dslContext.selectDistinct(
                APP_USER.APP_USER_ID,
                APP_USER.LOGIN_ID,
                APP_USER.NAME,
                APP_USER.IS_DEVELOPER.as("developer"),
                APP_USER.IS_ADMIN.as("admin"),
                APP_USER.IS_ENABLED.as("enabled"),
                APP_USER.ORGANIZATION,
                APP_OAUTH2_USER.APP_OAUTH2_USER_ID
        ).from(APP_USER)
                .leftJoin(APP_OAUTH2_USER).on(APP_USER.APP_USER_ID.eq(APP_OAUTH2_USER.APP_USER_ID))
                .leftJoin(USER_TENANT)
                .on(USER_TENANT.APP_USER_ID.eq(APP_USER.APP_USER_ID));

        List<Condition> conditions = new ArrayList();
        if (StringUtils.hasLength(request.getLoginId())) {
            conditions.add(APP_USER.LOGIN_ID.containsIgnoreCase(request.getLoginId().trim()));
        }
        if (StringUtils.hasLength(request.getName())) {
            conditions.add(APP_USER.NAME.containsIgnoreCase(request.getName().trim()));
        }
        if (StringUtils.hasLength(request.getOrganization())) {
            conditions.add(APP_USER.ORGANIZATION.containsIgnoreCase(request.getOrganization().trim()));
        }
        if (request.getEnabled() != null) {
            conditions.add(APP_USER.IS_ENABLED.eq((byte) (request.getEnabled() ? 1 : 0)));
        }
        List<Condition> roleConditions = new ArrayList();
        for (String role : request.getRoles()) {
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
        if (request.isExcludeSSO()) {
            conditions.add(APP_OAUTH2_USER.APP_OAUTH2_USER_ID.isNull());
        }
        Boolean excludeRequester = request.getExcludeRequester();
        if (excludeRequester != null && excludeRequester == true) {
            conditions.add(APP_USER.LOGIN_ID.notEqualIgnoreCase(sessionService.getAppUserByUsername(user).getLoginId().trim()));
        }

		if (configService.isTenantEnabled()) {
			BigInteger tenantId = request.getTenantId();
			boolean notConnectedToTenant = request.isNotConnectedToTenant();
			if (tenantId != null && !notConnectedToTenant) {
				conditions.add(USER_TENANT.TENANT_ID.eq(ULong.valueOf(tenantId)));
			}

			if (tenantId != null && notConnectedToTenant) {
				conditions.add(APP_USER.APP_USER_ID.notIn(dslContext.select(USER_TENANT.APP_USER_ID).from(USER_TENANT)
						.where(USER_TENANT.TENANT_ID.eq(ULong.valueOf(tenantId)))));
			}

			List<Long> businessCtxIds = request.getBusinessCtxIds();
			if (businessCtxIds != null && !businessCtxIds.isEmpty()) {
				conditions.add(USER_TENANT.TENANT_ID.in(dslContext.select(TENANT_BUSINESS_CTX.TENANT_ID)
						.from(TENANT_BUSINESS_CTX).where(TENANT_BUSINESS_CTX.BIZ_CTX_ID.in(businessCtxIds))));
			}
		}

        SelectConditionStep<Record6<ULong, String, String, Byte, String, ULong>> conditionStep = step.where(conditions);

        PageRequest pageRequest = request.getPageRequest();
        String sortDirection = pageRequest.getSortDirection();
        SortField sortField = null;
        switch (pageRequest.getSortActive()) {
            case "loginId":
                if ("asc".equals(sortDirection)) {
                    sortField = APP_USER.LOGIN_ID.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = APP_USER.LOGIN_ID.desc();
                }
                break;

            case "name":
                if ("asc".equals(sortDirection)) {
                    sortField = APP_USER.NAME.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = APP_USER.NAME.desc();
                }
                break;

            case "organization":
                if ("asc".equals(sortDirection)) {
                    sortField = APP_USER.ORGANIZATION.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = APP_USER.ORGANIZATION.desc();
                }
                break;

            case "status":
                if ("asc".equals(sortDirection)) {
                    sortField = APP_USER.IS_ENABLED.desc(); // 1 (Enable) to 0 (Disable)
                } else if ("desc".equals(sortDirection)) {
                    sortField = APP_USER.IS_ENABLED.asc(); // 0 (Disable) to 1 (Enable)
                }
                break;
        }

        SelectWithTiesAfterOffsetStep<Record6<ULong, String, String, Byte, String, ULong>> offsetStep = null;

        int pageCount = dslContext.fetchCount(conditionStep);
        if (sortField != null) {
            offsetStep = conditionStep.orderBy(sortField)
                    .limit(pageRequest.getOffset(), pageRequest.getPageSize());
        } else {
            if (pageRequest.getPageIndex() >= 0 && pageRequest.getPageSize() > 0) {
                offsetStep = conditionStep
                        .limit(pageRequest.getOffset(), pageRequest.getPageSize());
            }
        }

        List<AppUser> result = (offsetStep != null) ?
                offsetStep.fetchInto(AppUser.class) : conditionStep.fetchInto(AppUser.class);

        PageResponse<AppUser> response = new PageResponse();
        response.setList(result);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(pageCount);

        return response;
    }

    public AppUser getAccountById(long appUserId) {
        return getAccount(APP_USER.APP_USER_ID.eq(ULong.valueOf(appUserId)));
    }

    public AppUser getAccountByUsername(String username) {
        return getAccount(APP_USER.LOGIN_ID.eq(username));
    }

    private AppUser getAccount(Condition condition) {
        AppUser appUser = dslContext.select(
                APP_USER.APP_USER_ID,
                APP_USER.LOGIN_ID,
                APP_USER.NAME,
                APP_USER.IS_DEVELOPER.as("developer"),
                APP_USER.IS_ADMIN.as("admin"),
                APP_USER.IS_ENABLED.as("enabled"),
                APP_USER.ORGANIZATION,
                APP_OAUTH2_USER.APP_OAUTH2_USER_ID)
                .from(APP_USER)
                .leftJoin(APP_OAUTH2_USER)
                .on(APP_USER.APP_USER_ID.eq(APP_OAUTH2_USER.APP_USER_ID))
                .where(condition)
                .fetchOptionalInto(AppUser.class).orElse(null);
        if (appUser == null) {
            throw new AuthenticationCredentialsNotFoundException("An authentication information was not found.");
        }
        return appUser;

    }

    public List<String> getAccountLoginIds() {
        return dslContext.select(APP_USER.LOGIN_ID)
                .from(APP_USER)
                .fetchStreamInto(String.class)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    @Transactional
    public void insert(AuthenticatedPrincipal user, AppUser account) {
        org.oagi.score.service.common.data.AppUser appUser = sessionService.getAppUserByUsername(user);
        if (!appUser.isAdmin()) {
            throw new DataAccessForbiddenException("Only admin user can create a new account.");
        }

        AppUserRecord record = new AppUserRecord();
        record.setLoginId(account.getLoginId());
        if (account.getAppOauth2UserId() == null ||
            account.getAppOauth2UserId().longValue() == 0L) {
            record.setPassword(passwordEncoder.encode(account.getPassword()));
        }
        record.setName(account.getName());
        record.setOrganization(account.getOrganization());
        record.setIsDeveloper((byte) (account.isDeveloper() ? 1 : 0));
        record.setIsAdmin((byte) (account.isAdmin() ? 1 : 0));
        record.setIsEnabled((byte) 1);

        ULong appUserId = dslContext.insertInto(APP_USER)
                .set(record)
                .returning(APP_USER.APP_USER_ID).fetchOne().getAppUserId();

        if (account.getAppOauth2UserId() != null &&
            account.getAppOauth2UserId().longValue() > 0L &&
            account.getSub().length() > 0) {
            AppOauth2UserRecord oauth2User = dslContext.selectFrom(APP_OAUTH2_USER).where(
                    and(APP_OAUTH2_USER.APP_OAUTH2_USER_ID.eq(ULong.valueOf(account.getAppOauth2UserId())),
                            APP_OAUTH2_USER.SUB.eq(account.getSub()))).fetchOne();
            if (oauth2User == null) {
                throw new IllegalStateException("Cannot find Oauth2 account ");
            }
            oauth2User.setAppUserId(appUserId);
            oauth2User.update(APP_OAUTH2_USER.APP_USER_ID);
        }
    }

    public boolean hasTaken(String loginId) {
        return dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(loginId))
                .fetchOptionalInto(Long.class).orElse(0L) != 0L;
    }
}
