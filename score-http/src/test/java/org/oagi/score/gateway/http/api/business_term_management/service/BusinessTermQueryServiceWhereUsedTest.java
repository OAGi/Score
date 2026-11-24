package org.oagi.score.gateway.http.api.business_term_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationQueryService;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermUsageRecord;
import org.oagi.score.gateway.http.api.business_term_management.repository.BusinessTermQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Who may read the BIEs a business term is assigned to (Where Used): whoever may read business terms, the developer included,
 * and nobody when the tenant has the feature off. Creating and assigning stays closed to a developer.
 */
class BusinessTermQueryServiceWhereUsedTest {

    private static final BusinessTermId TERM = new BusinessTermId(BigInteger.valueOf(5));

    private BusinessTermQueryService service;
    private ApplicationConfigurationQueryService configuration;
    private BusinessTermQueryRepository query;
    private ScoreUser endUser;
    private ScoreUser developer;

    @BeforeEach
    void setUp() {
        service = new BusinessTermQueryService();
        var repositoryFactory = mock(RepositoryFactory.class);
        configuration = mock(ApplicationConfigurationQueryService.class);
        query = mock(BusinessTermQueryRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(service, "applicationConfigurationQueryService", configuration);
        when(repositoryFactory.businessTermQueryRepository(any())).thenReturn(query);
        when(configuration.isBusinessTermEnabled(any())).thenReturn(true);

        endUser = new ScoreUser(new UserId(BigInteger.valueOf(42)), "eu", "End User", "eu@example.com", true,
                List.of(ScoreRole.END_USER));
        developer = new ScoreUser(new UserId(BigInteger.valueOf(7)), "dev", "Developer", "dev@example.com", true,
                List.of(ScoreRole.DEVELOPER));
    }

    @Test
    void anEndUserGetsTheBiesOfTheTerm() {
        List<BusinessTermUsageRecord> usages = List.of(mock(BusinessTermUsageRecord.class));
        when(query.getBusinessTermUsageList(TERM)).thenReturn(usages);

        assertSame(usages, service.getBusinessTermUsageList(endUser, TERM));
    }

    @Test
    void aDeveloperGetsTheBiesOfTheTermToo() {
        List<BusinessTermUsageRecord> usages = List.of(mock(BusinessTermUsageRecord.class), mock(BusinessTermUsageRecord.class));
        when(query.getBusinessTermUsageList(TERM)).thenReturn(usages);

        assertEquals(2, service.getBusinessTermUsageList(developer, TERM).size());
    }

    @Test
    void nobodyGetsThemWhenTheFeatureIsOff() {
        when(configuration.isBusinessTermEnabled(any())).thenReturn(false);

        assertThrows(DataAccessForbiddenException.class, () -> service.getBusinessTermUsageList(endUser, TERM));
        assertThrows(DataAccessForbiddenException.class, () -> service.getBusinessTermUsageList(developer, TERM));
        verify(query, never()).getBusinessTermUsageList(any());
    }

    @Test
    void aDeveloperStillCannotCreateOrAssign() {
        assertThrows(DataAccessForbiddenException.class, () -> service.assertBusinessTermManageable(developer));
        assertThrows(DataAccessForbiddenException.class,
                () -> service.checkUniqueness(developer, TERM, "term", "http://example.com"));
    }

    @Test
    void anEndUserCanCreateAndAssign() {
        service.assertBusinessTermManageable(endUser);
    }
}
