package org.oagi.score.gateway.http.api.account_management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.account_management.controller.payload.AccountCreateRequest;
import org.oagi.score.gateway.http.api.account_management.controller.payload.UpdateAccountRequest;
import org.oagi.score.gateway.http.api.account_management.controller.payload.UpdateEmailRequest;
import org.oagi.score.gateway.http.api.account_management.controller.payload.UpdatePasswordRequest;
import org.oagi.score.gateway.http.api.account_management.model.AccountDetailsRecord;
import org.oagi.score.gateway.http.api.account_management.model.EmailValidationInfo;
import org.oagi.score.gateway.http.api.account_management.model.OAuth2UserRecord;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.mail.controller.payload.SendMailRequest;
import org.oagi.score.gateway.http.api.mail.service.MailService;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.springframework.util.StringUtils.hasLength;

@Service
@Transactional
public class AccountCommandService {

    private static long EMAIL_VALIDATION_EXPIRATION_TIME_IN_MINUTES = 10;

    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ApplicationConfigurationService configService;

    @Autowired
    private MailService mailService;

    public UserId create(ScoreUser requester, AccountCreateRequest request) {
        if (!requester.isAdministrator()) {
            throw new DataAccessForbiddenException("Only admin user can create a new account.");
        }

        var command = repositoryFactory.accountCommandRepository(requester, passwordEncoder);
        UserId appUserId = command.create(request.loginId(),
                (request.oAuth2UserId() == null) ? request.password() : null,
                request.name(),
                request.organization(),
                request.developer(),
                request.admin());

        if (request.oAuth2UserId() != null && hasLength(request.sub())) {
            var query = repositoryFactory.accountQueryRepository(requester);
            OAuth2UserRecord oAuth2UserRecord = query.getOAuth2User(request.oAuth2UserId(), request.sub());
            if (oAuth2UserRecord == null) {
                throw new IllegalStateException("Cannot find OAuth2 account");
            }
            command.linkOAuth2User(request.oAuth2UserId(), appUserId);
        }

        return appUserId;
    }

    public boolean update(ScoreUser requester, UpdateAccountRequest request) {

        if (!requester.isAdministrator()) {
            if (!requester.userId().equals(request.userId())) {
                throw new DataAccessForbiddenException("Only allowed to update the account by the owner.");
            }
        }

        var command = repositoryFactory.accountCommandRepository(requester, passwordEncoder);
        boolean updated = command.update(request.userId(),
                request.username(),
                request.organization(),
                request.admin(),
                hasLength(request.newPassword()) ? validate(request.newPassword()) : null);

        return updated;
    }

    public boolean updatePassword(ScoreUser requester, UpdatePasswordRequest request) {

        var query = repositoryFactory.accountQueryRepository(requester);
        if (!matches(request.oldPassword(), query.getEncodedPassword(requester.userId()))) {
            throw new IllegalArgumentException("Invalid old password");
        }

        String newPassword = validate(request.newPassword());
        var command = repositoryFactory.accountCommandRepository(requester, passwordEncoder);
        return command.updatePassword(newPassword);
    }

    private String validate(String password) {
        if (!StringUtils.hasLength(password) || password.length() < 5) {
            throw new IllegalArgumentException("Password must be at least 5 characters.");
        }
        return password;
    }

    private boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public boolean updateEmail(ScoreUser requester, UpdateEmailRequest request) {

        var query = repositoryFactory.accountQueryRepository(requester);
        AccountDetailsRecord accountDetails = query.getAccountDetails(requester.userId());
        if (Objects.equals(request.email(), accountDetails.email())) {
            return false;
        }

        var command = repositoryFactory.accountCommandRepository(requester, passwordEncoder);
        boolean updated = command.updateEmail(request.email());
        if (updated) {

            // reload
            requester = sessionService.getScoreUserByUserId(requester.userId());

            try {
                sendEmailValidationRequest(requester, request);
            } catch (IllegalStateException ignore) {
            }
        }

        return updated;
    }

    public void sendEmailValidationRequest(ScoreUser requester, UpdateEmailRequest request) {
        if (!configService.isFunctionsRequiringEmailTransmissionEnabled(requester)) {
            throw new IllegalStateException("The 'Functions requiring email transmission' feature is disabled. Please check the 'Application Settings'.");
        }

        var query = repositoryFactory.accountQueryRepository(requester);
        AccountDetailsRecord accountDetails = query.getAccountDetails(requester.userId());
        OAuth2UserRecord oAuth2User = query.getOAuth2User(accountDetails.userId());

        SendMailRequest sendMailRequest = new SendMailRequest();
        sendMailRequest.setTemplateName("email-validation");
        sendMailRequest.setRecipient(requester);
        sendMailRequest.setParameters(request.parameters());

        EmailValidationInfo validationInfo = new EmailValidationInfo(
                accountDetails.userId(), request.email(), new Date());

        byte[] bytes;
        try {
            bytes = new ObjectMapper().writeValueAsBytes(validationInfo);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to write an email validation info.", e);
        }

        BytesEncryptor encryptor = new AesBytesEncryptor(
                (oAuth2User != null) ? oAuth2User.sub() : query.getEncodedPassword(accountDetails.userId()),
                Hex.encodeHexString(accountDetails.loginId().getBytes()));
        byte[] encryptedObj = encryptor.encrypt(bytes);
        String q = Base64.encodeBase64String(encryptedObj);

        String emailValidationLink = (String) sendMailRequest.getParameters().get("email_validation_link");
        sendMailRequest.getParameters().put("email_validation_link",
                emailValidationLink + "?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8));
        mailService.sendMail(requester, sendMailRequest);
    }

    public void verifyEmailValidation(ScoreUser requester, String q) {

        var query = repositoryFactory.accountQueryRepository(requester);
        AccountDetailsRecord accountDetails = query.getAccountDetails(requester.userId());
        OAuth2UserRecord oAuth2User = query.getOAuth2User(accountDetails.userId());

        byte[] decryptedObj;
        try {
            BytesEncryptor encryptor = new AesBytesEncryptor(
                    (oAuth2User != null) ? oAuth2User.sub() : query.getEncodedPassword(accountDetails.userId()),
                    Hex.encodeHexString(accountDetails.loginId().getBytes()));
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

        if (!Objects.equals(emailValidationInfo.userId(), accountDetails.userId())) {
            throw new IllegalArgumentException("This request is not for the current user.");
        }

        if (!Objects.equals(emailValidationInfo.email(), accountDetails.email())) {
            throw new IllegalArgumentException("This request contains an invalid email.");
        }

        Date now = new Date();
        if (emailValidationInfo.timestamp().after(now)) {
            throw new IllegalArgumentException("This request is invalid.");
        }

        long diff = now.getTime() - emailValidationInfo.timestamp().getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes > EMAIL_VALIDATION_EXPIRATION_TIME_IN_MINUTES) {
            throw new IllegalArgumentException("This request has been expired.");
        }

        if (accountDetails.emailVerified()) {
            throw new IllegalArgumentException("The email address has already been verified.");
        }

        var command = repositoryFactory.accountCommandRepository(requester, passwordEncoder);
        command.setEmailVerified(accountDetails.userId(), true);
    }

    public void delinkOAuth2User(ScoreUser requester, UserId userId) {

        if (!requester.isAdministrator()) {
            throw new AccessControlException(requester);
        }

        var command = repositoryFactory.accountCommandRepository(requester, passwordEncoder);
        command.delinkOAuth2User(userId);
    }

    public boolean discard(ScoreUser requester, UserId userId) {

        if (!requester.isAdministrator()) {
            throw new AccessControlException(requester);
        }

        var command = repositoryFactory.accountCommandRepository(requester, passwordEncoder);
        return command.delete(userId);
    }

    public void setEnable(ScoreUser requester, UserId targetUserId, boolean enabled) {
        if (!requester.isDeveloper()) {
            throw new InsufficientAuthenticationException(
                    messages.getMessage(
                            "ExceptionTranslationFilter.insufficientAuthentication",
                            "Full authentication is required to access this resource"));
        }

        var query = repositoryFactory.accountQueryRepository(requester);
        AccountDetailsRecord targetAccountDetails = query.getAccountDetails(targetUserId);
        if (targetAccountDetails == null) {
            throw new IllegalArgumentException();
        }

        boolean prevEnabled = targetAccountDetails.enabled();
        if (prevEnabled != enabled) {
            var command = repositoryFactory.accountCommandRepository(requester, passwordEncoder);
            command.setEnabled(targetAccountDetails.userId(), enabled);
        }

        if (!enabled) {
            sessionService.invalidateByUsername(targetAccountDetails.loginId());
        }
    }

    @Transactional
    public void transferOwnership(ScoreUser requester, UserId targetUserId) {
        if (!requester.isAdministrator()) {
            throw new InsufficientAuthenticationException(
                    messages.getMessage(
                            "ExceptionTranslationFilter.insufficientAuthentication",
                            "Full authentication is required to access this resource"));
        }

        var query = repositoryFactory.accountQueryRepository(requester);
        AccountDetailsRecord targetAccountDetails = query.getAccountDetails(targetUserId);
        if (targetAccountDetails == null) {
            throw new IllegalArgumentException();
        }

        var command = repositoryFactory.accountCommandRepository(requester, passwordEncoder);
        command.updateOwnerUser(targetAccountDetails.userId());
    }

}
