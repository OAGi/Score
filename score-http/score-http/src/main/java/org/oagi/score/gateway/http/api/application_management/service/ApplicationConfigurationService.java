package org.oagi.score.gateway.http.api.application_management.service;

import org.oagi.score.gateway.http.api.application_management.data.ApplicationConfigurationChangeRequest;
import org.oagi.score.gateway.http.api.application_management.data.ApplicationSettingsInfo;
import org.oagi.score.gateway.http.api.application_management.data.SMTPSettingsInfo;
import org.oagi.score.gateway.http.api.mail.data.SendMailRequest;
import org.oagi.score.gateway.http.api.mail.service.MailService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.security.AccessControlException;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repo.component.app.configuration.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class ApplicationConfigurationService {

    @Autowired
    private ConfigurationRepository configRepo;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MailService mailService;

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    private static final String TENANT_CONFIG_PARAM_NAME = "score.tenant.enabled";

    private static final String BUSINESS_TERM_CONFIG_PARAM_NAME = "score.business-term.enabled";

    private static final String BIE_INVERSE_MODE_CONFIG_PARAM_NAME = "score.bie.inverse-mode";

    private static final String FUNCTIONS_REQUIRING_EMAIL_TRANSMISSION_CONFIG_PARAM_NAME =
            "score.functions-requiring-email-transmission.enabled";

    public static final String NAVBAR_BRAND_CONFIG_PARAM_NAME = "score.pages.navbar.brand";

    public static final String FAVICON_LINK_CONFIG_PARAM_NAME = "score.pages.favicon.link";

    public static final String SIGN_IN_PAGE_STATEMENT_CONFIG_PARAM_NAME = "score.pages.signin.statement";

    public static final String COMPONENT_STATE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(String state) {
        return "score.pages.colors.cc-state." + state + ".background";
    }

    public static final String COMPONENT_STATE_FONT_COLOR_CONFIG_PARAM_NAME(String state) {
        return "score.pages.colors.cc-state." + state + ".font";
    }

    public static final String RELEASE_STATE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(String state) {
        return "score.pages.colors.release-state." + state + ".background";
    }

    public static final String RELEASE_STATE_FONT_COLOR_CONFIG_PARAM_NAME(String state) {
        return "score.pages.colors.release-state." + state + ".font";
    }

    public static final String USER_ROLE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(String role) {
        return "score.pages.colors.user-role." + role + ".background";
    }

    public static final String USER_ROLE_FONT_COLOR_CONFIG_PARAM_NAME(String role) {
        return "score.pages.colors.user-role." + role + ".font";
    }

    public String getConfigurationValueByName(String paramConfigName) {
        return configRepo.getConfigurationValueByName(paramConfigName);
    }

    public boolean isTenantEnabled() {
        return getBooleanProperty(TENANT_CONFIG_PARAM_NAME);
    }

    public boolean isBusinessTermEnabled() {
        return getBooleanProperty(BUSINESS_TERM_CONFIG_PARAM_NAME);
    }

    public boolean isBieInverseModeEnabled() {
        return getBooleanProperty(BIE_INVERSE_MODE_CONFIG_PARAM_NAME);
    }

    public boolean isFunctionsRequiringEmailTransmissionEnabled() {
        return getBooleanProperty(FUNCTIONS_REQUIRING_EMAIL_TRANSMISSION_CONFIG_PARAM_NAME);
    }

    public boolean getBooleanProperty(String key) {
        return getBooleanProperty(key, false);
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        Boolean value = Boolean.valueOf(getProperty(key));
        return (value != null) ? value : defaultValue;
    }

    public int getIntProperty(String key) {
        return getIntProperty(key, 0);
    }

    public int getIntProperty(String key, int defaultValue) {
        Integer value = Integer.valueOf(getProperty(key));
        return (value != null) ? value : defaultValue;
    }

    public String getProperty(String key) {
        return configRepo.getConfigurationValueByName(key);
    }

    public void changeApplicationConfiguration(AuthenticatedPrincipal user,
                                               ApplicationConfigurationChangeRequest request) {
        ScoreUser scoreUser = sessionService.asScoreUser(user);
        if (!scoreUser.hasRole(ScoreRole.ADMINISTRATOR)) {
            throw new AccessControlException(scoreUser);
        }

        Boolean tenantEnabled = request.getTenantEnabled();
        if (tenantEnabled != null) {
            scoreRepositoryFactory.createConfigurationWriteRepository()
                    .upsertBooleanConfiguration(scoreUser, TENANT_CONFIG_PARAM_NAME, tenantEnabled);
        }

        Boolean businessTermEnabled = request.getBusinessTermEnabled();
        if (businessTermEnabled != null) {
            scoreRepositoryFactory.createConfigurationWriteRepository()
                    .upsertBooleanConfiguration(scoreUser, BUSINESS_TERM_CONFIG_PARAM_NAME, businessTermEnabled);
        }

        Boolean bieInverseModeEnabled = request.getBieInverseModeEnabled();
        if (bieInverseModeEnabled != null) {
            scoreRepositoryFactory.createConfigurationWriteRepository()
                    .upsertBooleanConfiguration(scoreUser, BIE_INVERSE_MODE_CONFIG_PARAM_NAME, bieInverseModeEnabled);
        }

        Boolean functionsRequiringEmailTransmissionEnabled = request.getFunctionsRequiringEmailTransmissionEnabled();
        if (functionsRequiringEmailTransmissionEnabled != null) {
            scoreRepositoryFactory.createConfigurationWriteRepository()
                    .upsertBooleanConfiguration(scoreUser, FUNCTIONS_REQUIRING_EMAIL_TRANSMISSION_CONFIG_PARAM_NAME,
                            functionsRequiringEmailTransmissionEnabled);
        }

        Map<String, String> keyValueMap = request.getKeyValueMap();
        if (keyValueMap != null && keyValueMap.size() > 0) {
            for (String key : keyValueMap.keySet()) {
                if (StringUtils.hasLength(key)) {
                    scoreRepositoryFactory.createConfigurationWriteRepository()
                            .upsertConfiguration(scoreUser, key, keyValueMap.get(key));
                }
            }
        }
    }

    public ApplicationSettingsInfo getApplicationSettingsInfo(ScoreUser requester) {
        ApplicationSettingsInfo applicationSettingsInfo = new ApplicationSettingsInfo();

        SMTPSettingsInfo smtpSettingsInfo = new SMTPSettingsInfo();
        smtpSettingsInfo.setHost(getProperty("score.mail.smtp.host"));
        smtpSettingsInfo.setPort(getIntProperty("score.mail.smtp.port"));
        smtpSettingsInfo.setAuth(getBooleanProperty("score.mail.smtp.auth"));
        smtpSettingsInfo.setSslEnable(getBooleanProperty("score.mail.smtp.ssl.enable"));
        smtpSettingsInfo.setStartTlsEnable(getBooleanProperty("score.mail.smtp.starttls.enable"));
        smtpSettingsInfo.setAuthMethod(getProperty("score.mail.smtp.auth.method"));
        smtpSettingsInfo.setAuthUsername(getProperty("score.mail.smtp.auth.username"));
        smtpSettingsInfo.setAuthPassword(getProperty("score.mail.smtp.auth.password"));
        applicationSettingsInfo.setSmtpSettingsInfo(smtpSettingsInfo);

        return applicationSettingsInfo;
    }

    public void updateApplicationSettingsInfo(ScoreUser requester, ApplicationSettingsInfo applicationSettingsInfo) {
        if (!requester.hasRole(ScoreRole.ADMINISTRATOR)) {
            throw new AccessControlException(requester);
        }

        SMTPSettingsInfo smtpSettingsInfo = applicationSettingsInfo.getSmtpSettingsInfo();
        if (smtpSettingsInfo != null) {
            configRepo.updateConfiguration(
                    "score.mail.smtp.host", smtpSettingsInfo.getHost());
            configRepo.updateConfiguration(
                    "score.mail.smtp.port", Integer.toString(smtpSettingsInfo.getPort()));
            configRepo.updateConfiguration(
                    "score.mail.smtp.auth", (smtpSettingsInfo.isAuth()) ? "true" : "false");
            configRepo.updateConfiguration(
                    "score.mail.smtp.ssl.enable", (smtpSettingsInfo.isSslEnable()) ? "true" : "false");
            configRepo.updateConfiguration(
                    "score.mail.smtp.starttls.enable", (smtpSettingsInfo.isStartTlsEnable()) ? "true" : "false");
            configRepo.updateConfiguration(
                    "score.mail.smtp.auth.method", smtpSettingsInfo.getAuthMethod());
            configRepo.updateConfiguration(
                    "score.mail.smtp.auth.username", smtpSettingsInfo.getAuthUsername());
            configRepo.updateConfiguration(
                    "score.mail.smtp.auth.password", smtpSettingsInfo.getAuthPassword());
        }
    }
}
