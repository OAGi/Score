package org.oagi.score.gateway.http.api.application_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.application_management.repository.ConfigurationQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** A configuration row that is missing gives the default the caller asked for. */
class ApplicationConfigurationQueryServiceDefaultsTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester",
            "tester@example.com", false, List.of(ScoreRole.END_USER));

    private ConfigurationQueryRepository query;
    private ApplicationConfigurationQueryService service;

    @BeforeEach
    void setUp() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        query = mock(ConfigurationQueryRepository.class);
        when(repositoryFactory.configurationQueryRepository(requester)).thenReturn(query);
        service = new ApplicationConfigurationQueryService();
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void aMissingBooleanGivesTheDefault(String stored) {
        when(query.getConfigurationValueByName("score.mail.smtp.auth")).thenReturn(stored);

        assertTrue(service.getBooleanProperty(requester, "score.mail.smtp.auth", true));
        assertFalse(service.getBooleanProperty(requester, "score.mail.smtp.auth", false));
        assertFalse(service.getBooleanProperty(requester, "score.mail.smtp.auth"));
    }

    @Test
    void aStoredBooleanWinsOverTheDefault() {
        when(query.getConfigurationValueByName("flag")).thenReturn("false");
        assertFalse(service.getBooleanProperty(requester, "flag", true));

        when(query.getConfigurationValueByName("flag")).thenReturn("true");
        assertTrue(service.getBooleanProperty(requester, "flag", false));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void aMissingIntGivesTheDefault(String stored) {
        when(query.getConfigurationValueByName("score.mail.smtp.port")).thenReturn(stored);

        assertEquals(587, service.getIntProperty(requester, "score.mail.smtp.port", 587));
        assertEquals(0, service.getIntProperty(requester, "score.mail.smtp.port"));
    }

    @Test
    void aStoredIntWinsOverTheDefault() {
        when(query.getConfigurationValueByName("score.mail.smtp.port")).thenReturn("2525");

        assertEquals(2525, service.getIntProperty(requester, "score.mail.smtp.port", 587));
    }
}
