package org.oagi.score.gateway.http.api.application_management.service;

import org.oagi.score.gateway.http.api.application_management.controller.payload.ApplicationConfigurationChangeRequest;
import org.oagi.score.gateway.http.api.application_management.model.ApplicationSettingsInfo;
import org.oagi.score.gateway.http.api.application_management.model.SMTPSettingsInfo;
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

import static org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationProperties.*;

/**
 * Performs mutating application-configuration operations.
 * <p>
 * This service owns command-side responsibilities such as writing configuration values,
 * validating admin-submitted filename expressions before persistence,
 * and applying feature-toggle changes.
 */
@Service
@Transactional
public class ApplicationConfigurationCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private FilenameExpressionValidationService filenameExpressionValidationService;

    /**
     * Applies boolean and key-value configuration changes contained in the request.
     *
     * @param requester user performing the update
     * @param request command payload containing one or more configuration changes
     */
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

    /**
     * Persists application settings after validating access and filename-expression inputs.
     *
     * @param requester user performing the update
     * @param applicationSettingsInfo settings payload to store
     */
    public void updateApplicationSettingsInfo(ScoreUser requester, ApplicationSettingsInfo applicationSettingsInfo) {
        if (!requester.hasRole(ScoreRole.ADMINISTRATOR)) {
            throw new AccessControlException(requester);
        }

        var command = repositoryFactory.configurationCommandRepository(requester);
        SMTPSettingsInfo smtpSettingsInfo = applicationSettingsInfo.getSmtpSettingsInfo();
        if (smtpSettingsInfo != null) {
            command.upsertConfiguration("score.mail.smtp.host", smtpSettingsInfo.getHost());
            command.upsertIntConfiguration("score.mail.smtp.port", smtpSettingsInfo.getPort());
            command.upsertBooleanConfiguration("score.mail.smtp.auth", smtpSettingsInfo.isAuth());
            command.upsertBooleanConfiguration("score.mail.smtp.ssl.enable", smtpSettingsInfo.isSslEnable());
            command.upsertBooleanConfiguration("score.mail.smtp.starttls.enable", smtpSettingsInfo.isStartTlsEnable());
            command.upsertConfiguration("score.mail.smtp.auth.method", smtpSettingsInfo.getAuthMethod());
            command.upsertConfiguration("score.mail.smtp.auth.username", smtpSettingsInfo.getAuthUsername());
            command.upsertConfiguration("score.mail.smtp.auth.password", smtpSettingsInfo.getAuthPassword());
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

    /**
     * Validates a filename expression pair for the requested target type.
     *
     * @param requester user requesting validation
     * @param type expression target type such as {@code bie-schema} or {@code bie-package-schema}
     * @param expression base filename expression
     * @param duplicateHandlerExpression duplicate filename handler expression
     */
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

}
