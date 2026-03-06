package org.oagi.score.gateway.http.api.application_management.service;

import org.oagi.score.gateway.http.api.application_management.controller.payload.ApplicationConfigurationChangeRequest;
import org.oagi.score.gateway.http.api.application_management.controller.payload.FilenameExpressionPreviewResponse;
import org.oagi.score.gateway.http.api.application_management.model.ApplicationSettingsInfo;
import org.oagi.score.gateway.http.api.application_management.model.SMTPSettingsInfo;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.ExpressionBasedFilenameStrategy;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.FilenameExpressionValidationService;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class ApplicationConfigurationService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private FilenameExpressionValidationService filenameExpressionValidationService;

    private static final String TENANT_CONFIG_PARAM_NAME = "score.tenant.enabled";

    private static final String BUSINESS_TERM_CONFIG_PARAM_NAME = "score.business-term.enabled";

    private static final String BIE_INVERSE_MODE_CONFIG_PARAM_NAME = "score.bie.inverse-mode";

    private static final String FUNCTIONS_REQUIRING_EMAIL_TRANSMISSION_CONFIG_PARAM_NAME =
            "score.functions-requiring-email-transmission.enabled";

    private static final String BROWSE_STANDARD_MODE_CONFIG_PARAM_NAME =
            "score.browse-standard-mode.enabled";

    public static final String BIE_SCHEMA_FILENAME_EXPRESSION_CONFIG_PARAM_NAME =
            "score.bie.schema-filename-expression";

    public static final String BIE_PACKAGE_SCHEMA_FILENAME_EXPRESSION_CONFIG_PARAM_NAME =
            "score.bie.package-schema-filename-expression";

    public static final String BIE_SCHEMA_FILENAME_DUPLICATE_HANDLER_EXPRESSION_CONFIG_PARAM_NAME =
            "score.bie.schema-filename-duplicate-handler-expression";

    public static final String BIE_PACKAGE_SCHEMA_FILENAME_DUPLICATE_HANDLER_EXPRESSION_CONFIG_PARAM_NAME =
            "score.bie.package-schema-filename-duplicate-handler-expression";

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

    public String getConfigurationValueByName(ScoreUser requester, String paramConfigName) {
        var query = repositoryFactory.configurationQueryRepository(requester);
        return query.getConfigurationValueByName(paramConfigName);
    }

    public boolean isTenantEnabled(ScoreUser requester) {
        return getBooleanProperty(requester, TENANT_CONFIG_PARAM_NAME);
    }

    public boolean isBusinessTermEnabled(ScoreUser requester) {
        return getBooleanProperty(requester, BUSINESS_TERM_CONFIG_PARAM_NAME);
    }

    public boolean isBieInverseModeEnabled(ScoreUser requester) {
        return getBooleanProperty(requester, BIE_INVERSE_MODE_CONFIG_PARAM_NAME);
    }

    public boolean isFunctionsRequiringEmailTransmissionEnabled(ScoreUser requester) {
        return getBooleanProperty(requester, FUNCTIONS_REQUIRING_EMAIL_TRANSMISSION_CONFIG_PARAM_NAME);
    }

    public boolean isBrowseStandardModeEnabled(ScoreUser requester) {
        return getBooleanProperty(requester, BROWSE_STANDARD_MODE_CONFIG_PARAM_NAME);
    }

    public boolean getBooleanProperty(ScoreUser requester, String key) {
        return getBooleanProperty(requester, key, false);
    }

    public boolean getBooleanProperty(ScoreUser requester, String key, boolean defaultValue) {
        Boolean value = Boolean.valueOf(getProperty(requester, key));
        return (value != null) ? value : defaultValue;
    }

    public int getIntProperty(ScoreUser requester, String key) {
        return getIntProperty(requester, key, 0);
    }

    public int getIntProperty(ScoreUser requester, String key, int defaultValue) {
        Integer value = Integer.valueOf(getProperty(requester, key));
        return (value != null) ? value : defaultValue;
    }

    public String getProperty(ScoreUser requester, String key) {
        var query = repositoryFactory.configurationQueryRepository(requester);
        return query.getConfigurationValueByName(key);
    }

    public void changeApplicationConfiguration(ScoreUser requester,
                                               ApplicationConfigurationChangeRequest request) {
        if (!requester.isAdministrator()) {
            throw new AccessControlException(requester);
        }

        var command = repositoryFactory.configurationCommandRepository(requester);

        Boolean tenantEnabled = request.getTenantEnabled();
        if (tenantEnabled != null) {
            command.upsertBooleanConfiguration(TENANT_CONFIG_PARAM_NAME, tenantEnabled);
        }

        Boolean businessTermEnabled = request.getBusinessTermEnabled();
        if (businessTermEnabled != null) {
            command.upsertBooleanConfiguration(BUSINESS_TERM_CONFIG_PARAM_NAME, businessTermEnabled);
        }

        Boolean bieInverseModeEnabled = request.getBieInverseModeEnabled();
        if (bieInverseModeEnabled != null) {
            command.upsertBooleanConfiguration(BIE_INVERSE_MODE_CONFIG_PARAM_NAME, bieInverseModeEnabled);
        }

        Boolean functionsRequiringEmailTransmissionEnabled = request.getFunctionsRequiringEmailTransmissionEnabled();
        if (functionsRequiringEmailTransmissionEnabled != null) {
            command.upsertBooleanConfiguration(FUNCTIONS_REQUIRING_EMAIL_TRANSMISSION_CONFIG_PARAM_NAME,
                            functionsRequiringEmailTransmissionEnabled);
        }

        Boolean browseStandardModeEnabled = request.getBrowseStandardModeEnabled();
        if (browseStandardModeEnabled != null) {
            command.upsertBooleanConfiguration(BROWSE_STANDARD_MODE_CONFIG_PARAM_NAME, browseStandardModeEnabled);
        }

        Map<String, String> keyValueMap = request.getKeyValueMap();
        if (keyValueMap != null && keyValueMap.size() > 0) {
            for (String key : keyValueMap.keySet()) {
                if (StringUtils.hasLength(key)) {
                    command.upsertConfiguration(key, keyValueMap.get(key));
                }
            }
        }
    }

    public ApplicationSettingsInfo getApplicationSettingsInfo(ScoreUser requester) {
        ApplicationSettingsInfo applicationSettingsInfo = new ApplicationSettingsInfo();

        SMTPSettingsInfo smtpSettingsInfo = new SMTPSettingsInfo();
        smtpSettingsInfo.setHost(getProperty(requester, "score.mail.smtp.host"));
        smtpSettingsInfo.setPort(getIntProperty(requester, "score.mail.smtp.port"));
        smtpSettingsInfo.setAuth(getBooleanProperty(requester, "score.mail.smtp.auth"));
        smtpSettingsInfo.setSslEnable(getBooleanProperty(requester, "score.mail.smtp.ssl.enable"));
        smtpSettingsInfo.setStartTlsEnable(getBooleanProperty(requester, "score.mail.smtp.starttls.enable"));
        smtpSettingsInfo.setAuthMethod(getProperty(requester, "score.mail.smtp.auth.method"));
        smtpSettingsInfo.setAuthUsername(getProperty(requester, "score.mail.smtp.auth.username"));
        smtpSettingsInfo.setAuthPassword(getProperty(requester, "score.mail.smtp.auth.password"));
        applicationSettingsInfo.setSmtpSettingsInfo(smtpSettingsInfo);
        applicationSettingsInfo.setBieSchemaFilenameExpression(getPropertyOrDefault(
                requester,
                BIE_SCHEMA_FILENAME_EXPRESSION_CONFIG_PARAM_NAME,
                ExpressionBasedFilenameStrategy.DEFAULT_BIE_SCHEMA_FILENAME_EXPRESSION));
        applicationSettingsInfo.setBiePackageSchemaFilenameExpression(getPropertyOrDefault(
                requester,
                BIE_PACKAGE_SCHEMA_FILENAME_EXPRESSION_CONFIG_PARAM_NAME,
                ExpressionBasedFilenameStrategy.DEFAULT_BIE_PACKAGE_SCHEMA_FILENAME_EXPRESSION));
        applicationSettingsInfo.setBieSchemaFilenameDuplicateHandlerExpression(getPropertyOrDefault(
                requester,
                BIE_SCHEMA_FILENAME_DUPLICATE_HANDLER_EXPRESSION_CONFIG_PARAM_NAME,
                ExpressionBasedFilenameStrategy.DEFAULT_BIE_SCHEMA_DUPLICATE_HANDLER_EXPRESSION));
        applicationSettingsInfo.setBiePackageSchemaFilenameDuplicateHandlerExpression(getPropertyOrDefault(
                requester,
                BIE_PACKAGE_SCHEMA_FILENAME_DUPLICATE_HANDLER_EXPRESSION_CONFIG_PARAM_NAME,
                ExpressionBasedFilenameStrategy.DEFAULT_BIE_PACKAGE_SCHEMA_DUPLICATE_HANDLER_EXPRESSION));

        return applicationSettingsInfo;
    }

    public void updateApplicationSettingsInfo(ScoreUser requester, ApplicationSettingsInfo applicationSettingsInfo) {
        if (!requester.hasRole(ScoreRole.ADMINISTRATOR)) {
            throw new AccessControlException(requester);
        }

        var command = repositoryFactory.configurationCommandRepository(requester);
        SMTPSettingsInfo smtpSettingsInfo = applicationSettingsInfo.getSmtpSettingsInfo();
        if (smtpSettingsInfo != null) {
            command.upsertConfiguration(
                    "score.mail.smtp.host", smtpSettingsInfo.getHost());
            command.upsertConfiguration(
                    "score.mail.smtp.port", Integer.toString(smtpSettingsInfo.getPort()));
            command.upsertConfiguration(
                    "score.mail.smtp.auth", (smtpSettingsInfo.isAuth()) ? "true" : "false");
            command.upsertConfiguration(
                    "score.mail.smtp.ssl.enable", (smtpSettingsInfo.isSslEnable()) ? "true" : "false");
            command.upsertConfiguration(
                    "score.mail.smtp.starttls.enable", (smtpSettingsInfo.isStartTlsEnable()) ? "true" : "false");
            command.upsertConfiguration(
                    "score.mail.smtp.auth.method", smtpSettingsInfo.getAuthMethod());
            command.upsertConfiguration(
                    "score.mail.smtp.auth.username", smtpSettingsInfo.getAuthUsername());
            command.upsertConfiguration(
                    "score.mail.smtp.auth.password", smtpSettingsInfo.getAuthPassword());
        }

        if (applicationSettingsInfo.getBieSchemaFilenameExpression() != null) {
            String bieSchemaFilenameExpression = StringUtils.trim(applicationSettingsInfo.getBieSchemaFilenameExpression());
            String bieSchemaFilenameDuplicateHandlerExpression = StringUtils.trim(
                    applicationSettingsInfo.getBieSchemaFilenameDuplicateHandlerExpression());
            filenameExpressionValidationService.validateBieSchemaExpression(
                    bieSchemaFilenameExpression,
                    bieSchemaFilenameDuplicateHandlerExpression);
            command.upsertConfiguration(
                    BIE_SCHEMA_FILENAME_EXPRESSION_CONFIG_PARAM_NAME,
                    bieSchemaFilenameExpression);
            command.upsertConfiguration(
                    BIE_SCHEMA_FILENAME_DUPLICATE_HANDLER_EXPRESSION_CONFIG_PARAM_NAME,
                    bieSchemaFilenameDuplicateHandlerExpression);
        }

        if (applicationSettingsInfo.getBiePackageSchemaFilenameExpression() != null) {
            String biePackageSchemaFilenameExpression = StringUtils.trim(applicationSettingsInfo.getBiePackageSchemaFilenameExpression());
            String biePackageSchemaFilenameDuplicateHandlerExpression = StringUtils.trim(
                    applicationSettingsInfo.getBiePackageSchemaFilenameDuplicateHandlerExpression());
            filenameExpressionValidationService.validateBiePackageSchemaExpression(
                    biePackageSchemaFilenameExpression,
                    biePackageSchemaFilenameDuplicateHandlerExpression);
            command.upsertConfiguration(
                    BIE_PACKAGE_SCHEMA_FILENAME_EXPRESSION_CONFIG_PARAM_NAME,
                    biePackageSchemaFilenameExpression);
            command.upsertConfiguration(
                    BIE_PACKAGE_SCHEMA_FILENAME_DUPLICATE_HANDLER_EXPRESSION_CONFIG_PARAM_NAME,
                    biePackageSchemaFilenameDuplicateHandlerExpression);
        }
    }

    public void validateFilenameExpression(ScoreUser requester,
                                           String type,
                                           String expression,
                                           String duplicateHandlerExpression) {
        if (!requester.hasRole(ScoreRole.ADMINISTRATOR)) {
            throw new AccessControlException(requester);
        }

        String normalizedType = StringUtils.trim(type).toLowerCase();
        String normalizedExpression = StringUtils.trim(expression);
        String normalizedDuplicateHandlerExpression = StringUtils.trim(duplicateHandlerExpression);
        switch (normalizedType) {
            case "bie-schema":
                filenameExpressionValidationService.validateBieSchemaExpression(
                        normalizedExpression, normalizedDuplicateHandlerExpression);
                break;
            case "bie-package-schema":
                filenameExpressionValidationService.validateBiePackageSchemaExpression(
                        normalizedExpression, normalizedDuplicateHandlerExpression);
                break;
            default:
                throw new IllegalArgumentException("Unregistered type: " + type);
        }
    }

    public FilenameExpressionPreviewResponse previewFilenameExpression(ScoreUser requester,
                                                                      String type,
                                                                      String expression,
                                                                      String duplicateHandlerExpression) {
        if (!requester.hasRole(ScoreRole.ADMINISTRATOR)) {
            throw new AccessControlException(requester);
        }

        String normalizedType = StringUtils.trim(type).toLowerCase();
        String normalizedExpression = StringUtils.trim(expression);
        String normalizedDuplicateHandlerExpression = StringUtils.trim(duplicateHandlerExpression);
        FilenameExpressionValidationService.PreviewResult previewResult;
        switch (normalizedType) {
            case "bie-schema":
                previewResult = filenameExpressionValidationService.previewBieSchemaExpression(
                        normalizedExpression, normalizedDuplicateHandlerExpression);
                break;
            case "bie-package-schema":
                previewResult = filenameExpressionValidationService.previewBiePackageSchemaExpression(
                        normalizedExpression, normalizedDuplicateHandlerExpression);
                break;
            default:
                throw new IllegalArgumentException("Unregistered type: " + type);
        }
        return new FilenameExpressionPreviewResponse(
                previewResult.sampleFilename(),
                previewResult.sampleDuplicateFilename());
    }

    private String getPropertyOrDefault(ScoreUser requester, String key, String defaultValue) {
        String value = StringUtils.trim(getProperty(requester, key));
        if (!StringUtils.hasLength(value)) {
            return defaultValue;
        }
        return value;
    }
}
