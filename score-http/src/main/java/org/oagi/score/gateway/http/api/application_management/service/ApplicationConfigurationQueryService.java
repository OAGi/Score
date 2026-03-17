package org.oagi.score.gateway.http.api.application_management.service;

import org.oagi.score.gateway.http.api.application_management.controller.payload.FilenameExpressionPreviewResponse;
import org.oagi.score.gateway.http.api.application_management.model.ApplicationSettingsInfo;
import org.oagi.score.gateway.http.api.application_management.model.SMTPSettingsInfo;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.ExpressionBasedFilenameStrategy;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.FilenameExpressionValidationService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationProperties.*;

/**
 * Provides read-only access to application configuration data.
 * <p>
 * This service resolves persisted configuration values, exposes convenience accessors
 * for frequently-used feature flags and SMTP settings, and generates filename previews.
 */
@Service
@Transactional(readOnly = true)
public class ApplicationConfigurationQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private FilenameExpressionValidationService filenameExpressionValidationService;

    /**
     * Returns a raw configuration value by its persisted parameter name.
     *
     * @param requester user requesting the value
     * @param paramConfigName configuration parameter name
     * @return stored configuration value, or {@code null} when absent
     */
    public String getConfigurationValueByName(ScoreUser requester, String paramConfigName) {
        var query = repositoryFactory.configurationQueryRepository(requester);
        return query.getConfigurationValueByName(paramConfigName);
    }

    /**
     * Indicates whether tenant mode is enabled.
     *
     * @param requester user requesting the value
     * @return {@code true} when tenant mode is enabled
     */
    public boolean isTenantEnabled(ScoreUser requester) {
        return getBooleanProperty(requester, TENANT_CONFIG_PARAM_NAME);
    }

    /**
     * Indicates whether business-term support is enabled.
     *
     * @param requester user requesting the value
     * @return {@code true} when business-term support is enabled
     */
    public boolean isBusinessTermEnabled(ScoreUser requester) {
        return getBooleanProperty(requester, BUSINESS_TERM_CONFIG_PARAM_NAME);
    }

    /**
     * Indicates whether BIE inverse mode is enabled.
     *
     * @param requester user requesting the value
     * @return {@code true} when BIE inverse mode is enabled
     */
    public boolean isBieInverseModeEnabled(ScoreUser requester) {
        return getBooleanProperty(requester, BIE_INVERSE_MODE_CONFIG_PARAM_NAME);
    }

    /**
     * Indicates whether email-driven features are enabled.
     *
     * @param requester user requesting the value
     * @return {@code true} when email-driven features are enabled
     */
    public boolean isFunctionsRequiringEmailTransmissionEnabled(ScoreUser requester) {
        return getBooleanProperty(requester, FUNCTIONS_REQUIRING_EMAIL_TRANSMISSION_CONFIG_PARAM_NAME);
    }

    /**
     * Indicates whether Browse Standard mode is enabled.
     *
     * @param requester user requesting the value
     * @return {@code true} when Browse Standard mode is enabled
     */
    public boolean isBrowseStandardModeEnabled(ScoreUser requester) {
        return getBooleanProperty(requester, BROWSE_STANDARD_MODE_CONFIG_PARAM_NAME);
    }

    /**
     * Reads a boolean configuration value with a default of {@code false}.
     *
     * @param requester user requesting the value
     * @param key configuration parameter name
     * @return resolved boolean value
     */
    public boolean getBooleanProperty(ScoreUser requester, String key) {
        return getBooleanProperty(requester, key, false);
    }

    /**
     * Reads a boolean configuration value with the supplied fallback.
     *
     * @param requester user requesting the value
     * @param key configuration parameter name
     * @param defaultValue fallback value when the property is absent
     * @return resolved boolean value
     */
    public boolean getBooleanProperty(ScoreUser requester, String key, boolean defaultValue) {
        Boolean value = Boolean.valueOf(getProperty(requester, key));
        return (value != null) ? value : defaultValue;
    }

    /**
     * Reads an integer configuration value with a default of {@code 0}.
     *
     * @param requester user requesting the value
     * @param key configuration parameter name
     * @return resolved integer value
     */
    public int getIntProperty(ScoreUser requester, String key) {
        return getIntProperty(requester, key, 0);
    }

    /**
     * Reads an integer configuration value with the supplied fallback.
     *
     * @param requester user requesting the value
     * @param key configuration parameter name
     * @param defaultValue fallback value when the property is absent
     * @return resolved integer value
     */
    public int getIntProperty(ScoreUser requester, String key, int defaultValue) {
        Integer value = Integer.valueOf(getProperty(requester, key));
        return (value != null) ? value : defaultValue;
    }

    /**
     * Returns a raw configuration property value.
     *
     * @param requester user requesting the value
     * @param key configuration parameter name
     * @return stored configuration value, or {@code null} when absent
     */
    public String getProperty(ScoreUser requester, String key) {
        var query = repositoryFactory.configurationQueryRepository(requester);
        return query.getConfigurationValueByName(key);
    }

    /**
     * Builds the application-settings response from persisted configuration values.
     *
     * @param requester user requesting the settings
     * @return aggregated application settings
     */
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

    /**
     * Produces sample filenames for a candidate expression pair.
     *
     * @param requester user requesting the preview
     * @param type expression target type such as {@code bie-schema} or {@code bie-package-schema}
     * @param expression base filename expression
     * @param duplicateHandlerExpression duplicate filename handler expression
     * @return preview response containing sample filename values
     */
    public FilenameExpressionPreviewResponse previewFilenameExpression(
            ScoreUser requester, String type, String expression, String duplicateHandlerExpression) {
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
