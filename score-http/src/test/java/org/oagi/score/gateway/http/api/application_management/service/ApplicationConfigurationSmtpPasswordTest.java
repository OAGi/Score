package org.oagi.score.gateway.http.api.application_management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.application_management.model.ApplicationSettingsInfo;
import org.oagi.score.gateway.http.api.application_management.model.SMTPSettingsInfo;
import org.oagi.score.gateway.http.api.application_management.repository.ConfigurationCommandRepository;
import org.oagi.score.gateway.http.api.application_management.repository.ConfigurationQueryRepository;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Issue #304: the SMTP settings are for administrators and the stored SMTP password never leaves the server. */
class ApplicationConfigurationSmtpPasswordTest {

    private static final String PASSWORD_KEY = "score.mail.smtp.auth.password";

    private final ScoreUser admin = new ScoreUser(new UserId(BigInteger.ONE), "admin", "Admin",
            "admin@example.com", true, List.of(ScoreRole.ADMINISTRATOR));
    private final ScoreUser endUser = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester",
            "tester@example.com", false, List.of(ScoreRole.END_USER));

    private ConfigurationQueryRepository query;
    private ConfigurationCommandRepository command;
    private ApplicationConfigurationQueryService queryService;
    private ApplicationConfigurationCommandService commandService;

    @BeforeEach
    void setUp() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        query = mock(ConfigurationQueryRepository.class);
        command = mock(ConfigurationCommandRepository.class);
        when(repositoryFactory.configurationQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.configurationCommandRepository(any())).thenReturn(command);

        queryService = new ApplicationConfigurationQueryService();
        ReflectionTestUtils.setField(queryService, "repositoryFactory", repositoryFactory);
        commandService = new ApplicationConfigurationCommandService();
        ReflectionTestUtils.setField(commandService, "repositoryFactory", repositoryFactory);
    }

    private ApplicationSettingsInfo settingsWithPassword(String password) {
        SMTPSettingsInfo smtp = new SMTPSettingsInfo();
        smtp.setHost("smtp.example.com");
        smtp.setAuthPassword(password);
        ApplicationSettingsInfo info = new ApplicationSettingsInfo();
        info.setSmtpSettingsInfo(smtp);
        return info;
    }

    @Test
    void aNonAdministratorCannotReadTheSettings() {
        assertThrows(AccessControlException.class, () -> queryService.getApplicationSettingsInfo(endUser));
        verifyNoInteractions(query);
    }

    @Test
    void anAdministratorIsToldThatAPasswordIsStoredButNotWhat() throws Exception {
        when(query.getConfigurationValueByName(PASSWORD_KEY)).thenReturn("s3cret");

        ApplicationSettingsInfo info = queryService.getApplicationSettingsInfo(admin);

        assertTrue(info.getSmtpSettingsInfo().isAuthPasswordSet());
        String json = new ObjectMapper().writeValueAsString(info);
        assertFalse(json.contains("s3cret"), json);
        assertFalse(json.contains("\"authPassword\""), json);
        assertTrue(json.contains("\"authPasswordSet\":true"), json);
    }

    @Test
    void noStoredPasswordIsReportedAsNotSet() {
        when(query.getConfigurationValueByName(PASSWORD_KEY)).thenReturn(null);
        assertFalse(queryService.getApplicationSettingsInfo(admin).getSmtpSettingsInfo().isAuthPasswordSet());

        when(query.getConfigurationValueByName(PASSWORD_KEY)).thenReturn("");
        assertFalse(queryService.getApplicationSettingsInfo(admin).getSmtpSettingsInfo().isAuthPasswordSet());
    }

    @Test
    void theRequestBodyStillCarriesThePasswordButNotTheFlag() throws Exception {
        SMTPSettingsInfo smtp = new ObjectMapper().readValue(
                "{\"authPassword\":\"new\",\"authPasswordSet\":true}", SMTPSettingsInfo.class);

        assertEquals("new", smtp.getAuthPassword());
        assertFalse(smtp.isAuthPasswordSet());
    }

    @Test
    void anUpdateWithoutAPasswordKeepsTheStoredOne() {
        commandService.updateApplicationSettingsInfo(admin, settingsWithPassword(null));

        verify(command).upsertConfiguration(eq("score.mail.smtp.host"), any());
        verify(command, never()).upsertConfiguration(eq(PASSWORD_KEY), any());
    }

    @Test
    void anEmptyPasswordClearsTheStoredOne() {
        commandService.updateApplicationSettingsInfo(admin, settingsWithPassword(""));

        verify(command).upsertConfiguration(PASSWORD_KEY, "");
    }

    @Test
    void aGivenPasswordReplacesTheStoredOne() {
        commandService.updateApplicationSettingsInfo(admin, settingsWithPassword("new"));

        verify(command).upsertConfiguration(PASSWORD_KEY, "new");
    }

    @Test
    void aNonAdministratorCannotUpdateTheSettings() {
        assertThrows(AccessControlException.class,
                () -> commandService.updateApplicationSettingsInfo(endUser, settingsWithPassword("new")));
        verifyNoInteractions(command);
    }
}
