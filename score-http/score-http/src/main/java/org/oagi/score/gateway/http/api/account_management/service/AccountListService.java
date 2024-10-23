package org.oagi.score.gateway.http.api.account_management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.account_management.data.AccountListRequest;
import org.oagi.score.gateway.http.api.account_management.data.AccountUpdateRequest;
import org.oagi.score.gateway.http.api.account_management.data.AppUser;
import org.oagi.score.gateway.http.api.account_management.data.EmailValidationInfo;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.mail.data.SendMailRequest;
import org.oagi.score.gateway.http.api.mail.service.MailService;
import org.oagi.score.gateway.http.api.tenant_management.service.TenantService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AppOauth2UserRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AppUserRecord;
import org.oagi.score.repo.api.security.AccessControlException;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.or;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class AccountListService {

    private static long EMAIL_VALIDATION_EXPIRATION_TIME_IN_MINUTES = 10;

    @Autowired
    private MailService mailService;

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
        SortField sortField = getSortField(pageRequest);

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
    private SortField getSortField(PageRequest pageRequest) {
        Field field = null;
        SortField sortField = null;
        String sortDirection = pageRequest.getSortDirection();
        if (StringUtils.hasLength(pageRequest.getSortActive())) {
            switch (pageRequest.getSortActive()) {
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
            }
        }

        if (field != null) {
            if ("asc".equals(sortDirection)) {
                sortField = field.asc();
            } else if ("desc".equals(sortDirection)) {
                sortField = field.desc();
            }
        }
        return sortField;
    }

    public AppUser getAccountById(BigInteger appUserId) {
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
                .on(APP_OAUTH2_USER.OAUTH2_APP_ID.eq(OAUTH2_APP.OAUTH2_APP_ID))
                .where(condition)
                .fetchOptionalInto(AppUser.class).orElse(null);
        if (appUser == null) {
            throw new AuthenticationCredentialsNotFoundException("An authentication information was not found.");
        }

        ULong appUserId = ULong.valueOf(appUser.getAppUserId());
        boolean hasData = (dslContext.selectCount()
                .from(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(appUserId))
                .fetchOptionalInto(Integer.class).orElse(0) + dslContext.selectCount()
                .from(ACC)
                .where(or(
                        ACC.OWNER_USER_ID.eq(appUserId),
                        ACC.CREATED_BY.eq(appUserId),
                        ACC.LAST_UPDATED_BY.eq(appUserId)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) + dslContext.selectCount()
                .from(ASCC)
                .where(or(
                        ASCC.OWNER_USER_ID.eq(appUserId),
                        ASCC.CREATED_BY.eq(appUserId),
                        ASCC.LAST_UPDATED_BY.eq(appUserId)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) + dslContext.selectCount()
                .from(BCC)
                .where(or(
                        BCC.OWNER_USER_ID.eq(appUserId),
                        BCC.CREATED_BY.eq(appUserId),
                        BCC.LAST_UPDATED_BY.eq(appUserId)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) + dslContext.selectCount()
                .from(ASCCP)
                .where(or(
                        ASCCP.OWNER_USER_ID.eq(appUserId),
                        ASCCP.CREATED_BY.eq(appUserId),
                        ASCCP.LAST_UPDATED_BY.eq(appUserId)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) + dslContext.selectCount()
                .from(BCCP)
                .where(or(
                        BCCP.OWNER_USER_ID.eq(appUserId),
                        BCCP.CREATED_BY.eq(appUserId),
                        BCCP.LAST_UPDATED_BY.eq(appUserId)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) + dslContext.selectCount()
                .from(DT)
                .where(or(
                        DT.OWNER_USER_ID.eq(appUserId),
                        DT.CREATED_BY.eq(appUserId),
                        DT.LAST_UPDATED_BY.eq(appUserId)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) + dslContext.selectCount()
                .from(CODE_LIST)
                .where(or(
                        CODE_LIST.OWNER_USER_ID.eq(appUserId),
                        CODE_LIST.CREATED_BY.eq(appUserId),
                        CODE_LIST.LAST_UPDATED_BY.eq(appUserId)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) + dslContext.selectCount()
                .from(AGENCY_ID_LIST)
                .where(or(
                        AGENCY_ID_LIST.OWNER_USER_ID.eq(appUserId),
                        AGENCY_ID_LIST.CREATED_BY.eq(appUserId),
                        AGENCY_ID_LIST.LAST_UPDATED_BY.eq(appUserId)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) + dslContext.selectCount()
                .from(CTX_CATEGORY)
                .where(or(
                        CTX_CATEGORY.CREATED_BY.eq(appUserId),
                        CTX_CATEGORY.LAST_UPDATED_BY.eq(appUserId)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) + dslContext.selectCount()
                .from(CTX_SCHEME)
                .where(or(
                        CTX_SCHEME.CREATED_BY.eq(appUserId),
                        CTX_SCHEME.LAST_UPDATED_BY.eq(appUserId)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) + dslContext.selectCount()
                .from(BIZ_CTX)
                .where(or(
                        BIZ_CTX.CREATED_BY.eq(appUserId),
                        BIZ_CTX.LAST_UPDATED_BY.eq(appUserId)
                ))
                .fetchOptionalInto(Integer.class).orElse(0)
        ) > 0;
        appUser.setHasData(hasData);

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
    public void update(ScoreUser requester, AccountUpdateRequest request) {
        AppUserRecord appUserRecord = dslContext.selectFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(requester.getUserId())))
                .fetchOne();
        if (Objects.equals(request.getEmail(), appUserRecord.getEmail())) {
            return;
        }

        dslContext.update(APP_USER)
                .set(APP_USER.EMAIL, request.getEmail())
                .set(APP_USER.EMAIL_VERIFIED, (byte) 0)
                .setNull(APP_USER.EMAIL_VERIFIED_TIMESTAMP)
                .where(APP_USER.APP_USER_ID.eq(appUserRecord.getAppUserId()))
                .execute();

        requester.setEmailAddress(request.getEmail());
        requester.setEmailVerified(false);

        try {
            sendEmailValidationRequest(requester, request);
        } catch (IllegalStateException ignore) {
        }
    }

    @Transactional
    public void sendEmailValidationRequest(ScoreUser requester, AccountUpdateRequest request) {
        if (!configService.isFunctionsRequiringEmailTransmissionEnabled()) {
            throw new IllegalStateException("The 'Functions requiring email transmission' feature is disabled. Please check the 'Application Settings'.");
        }

        AppUserRecord appUserRecord = dslContext.selectFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(requester.getUserId())))
                .fetchOne();
        AppOauth2UserRecord appOauth2UserRecord = dslContext.selectFrom(APP_OAUTH2_USER)
                .where(APP_OAUTH2_USER.APP_USER_ID.eq(appUserRecord.getAppUserId()))
                .fetchOptional().orElse(null);

        SendMailRequest sendMailRequest = new SendMailRequest();
        sendMailRequest.setTemplateName("email-validation");
        sendMailRequest.setRecipient(requester);
        sendMailRequest.setParameters(request.getParameters());

        EmailValidationInfo validationInfo = new EmailValidationInfo();
        validationInfo.setAppUserId(appUserRecord.getAppUserId().toBigInteger());
        validationInfo.setEmail(request.getEmail());
        validationInfo.setTimestamp(new Date());

        byte[] bytes;
        try {
            bytes = new ObjectMapper().writeValueAsBytes(validationInfo);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to write an email validation info.", e);
        }

        BytesEncryptor encryptor = new AesBytesEncryptor(
                (appOauth2UserRecord != null) ? appOauth2UserRecord.getSub() : appUserRecord.getPassword(),
                Hex.encodeHexString(appUserRecord.getLoginId().getBytes()));
        byte[] encryptedObj = encryptor.encrypt(bytes);
        String q = Base64.encodeBase64String(encryptedObj);

        String emailValidationLink = (String) sendMailRequest.getParameters().get("email_validation_link");
        sendMailRequest.getParameters().put("email_validation_link",
                emailValidationLink + "?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8));
        mailService.sendMail(requester, sendMailRequest);
    }

    @Transactional
    public void verifyEmailValidation(ScoreUser requester, String q) {
        AppUserRecord appUserRecord = dslContext.selectFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(requester.getUserId())))
                .fetchOne();
        AppOauth2UserRecord appOauth2UserRecord = dslContext.selectFrom(APP_OAUTH2_USER)
                .where(APP_OAUTH2_USER.APP_USER_ID.eq(appUserRecord.getAppUserId()))
                .fetchOptional().orElse(null);

        byte[] decryptedObj;
        try {
            BytesEncryptor encryptor = new AesBytesEncryptor(
                    (appOauth2UserRecord != null) ? appOauth2UserRecord.getSub() : appUserRecord.getPassword(),
                    Hex.encodeHexString(appUserRecord.getLoginId().getBytes()));
            byte[] decQ = Base64.decodeBase64(q);
            decryptedObj = encryptor.decrypt(decQ);
        } catch (Exception e) {
            throw new IllegalArgumentException("The request does not seem to be intended for the current user or contains incorrect information.", e);
        }

        EmailValidationInfo emailValidationInfo;
        try {
            emailValidationInfo = new ObjectMapper().readValue(decryptedObj, EmailValidationInfo.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read an email validation info.", e);
        }

        if (!Objects.equals(emailValidationInfo.getAppUserId(), appUserRecord.getAppUserId().toBigInteger())) {
            throw new IllegalArgumentException("This request is not for the current user.");
        }

        if (!Objects.equals(emailValidationInfo.getEmail(), appUserRecord.getEmail())) {
            throw new IllegalArgumentException("This request contains an invalid email.");
        }

        Date now = new Date();
        if (emailValidationInfo.getTimestamp().after(now)) {
            throw new IllegalArgumentException("This request is invalid.");
        }

        long diff = now.getTime() - emailValidationInfo.getTimestamp().getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes > EMAIL_VALIDATION_EXPIRATION_TIME_IN_MINUTES) {
            throw new IllegalArgumentException("This request has been expired.");
        }

        if ((byte) 1 == appUserRecord.getEmailVerified()) {
            throw new IllegalArgumentException("The email address has already been verified.");
        }

        dslContext.update(APP_USER)
                .set(APP_USER.EMAIL_VERIFIED, (byte) 1)
                .set(APP_USER.EMAIL_VERIFIED_TIMESTAMP, LocalDateTime.now())
                .where(APP_USER.APP_USER_ID.eq(appUserRecord.getAppUserId()))
                .execute();
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

    @Transactional
    public void delinkOAuth2AppUser(AuthenticatedPrincipal user, BigInteger appUserId) {
        ScoreUser requester = sessionService.asScoreUser(user);
        if (!requester.hasRole(ScoreRole.ADMINISTRATOR)) {
            throw new AccessControlException(requester);
        }
        dslContext.update(APP_OAUTH2_USER)
                .setNull(APP_OAUTH2_USER.APP_USER_ID)
                .where(APP_OAUTH2_USER.APP_USER_ID.eq(ULong.valueOf(appUserId)))
                .execute();
    }

    @Transactional
    public void removeUser(AuthenticatedPrincipal user, BigInteger appUserId) {
        ScoreUser requester = sessionService.asScoreUser(user);
        if (!requester.hasRole(ScoreRole.ADMINISTRATOR)) {
            throw new AccessControlException(requester);
        }

        dslContext.deleteFrom(APP_OAUTH2_USER)
                .where(APP_OAUTH2_USER.APP_USER_ID.eq(ULong.valueOf(appUserId)))
                .execute();
        dslContext.deleteFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(appUserId)))
                .execute();
    }


    public static void main(String[] args) {
        AppUserRecord appUserRecord = new AppUserRecord();
        appUserRecord.setAppUserId(ULong.valueOf(100000002));
        appUserRecord.setLoginId("test2");
        appUserRecord.setPassword("$2a$10$oarLmPPWgWAjpVuE73bdMuLq48Df1H94lTDtp4nMyJlvyjL7Br3f6");

        EmailValidationInfo validationInfo = new EmailValidationInfo();
        validationInfo.setAppUserId(appUserRecord.getAppUserId().toBigInteger());
        validationInfo.setEmail("hakju.oh@gmail.com");
        validationInfo.setTimestamp(new Date());

        byte[] bytes;
        try {
            bytes = new ObjectMapper().writeValueAsBytes(validationInfo);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to write an email validation info.", e);
        }

        BytesEncryptor encryptor = new AesBytesEncryptor(appUserRecord.getPassword(),
                Hex.encodeHexString(appUserRecord.getLoginId().getBytes()));
        byte[] encryptedObj = encryptor.encrypt(bytes);
        String q = Base64.encodeBase64String(encryptedObj);

        System.out.println(q);
    }
}
