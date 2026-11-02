package org.oagi.score.gateway.http.api.cc_management.service;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.AccCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.AsccpQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The derivation check of an ACC purge must look at the ACC's own derived ACCs, not at its base ACC's.
 */
class CcCommandServicePurgeAccTest {

    private static final AccManifestId ACC_MANIFEST_ID = new AccManifestId(BigInteger.TEN);
    private static final AccManifestId BASE_ACC_MANIFEST_ID = new AccManifestId(BigInteger.ONE);

    private CcCommandService service;
    private AccQueryRepository accQuery;
    private AccCommandRepository accCommand;

    @BeforeEach
    void setUp() {
        service = new CcCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        accQuery = mock(AccQueryRepository.class);
        accCommand = mock(AccCommandRepository.class);
        AsccpQueryRepository asccpQuery = mock(AsccpQueryRepository.class);

        AccSummaryRecord acc = mock(AccSummaryRecord.class);
        when(acc.state()).thenReturn(CcState.Deleted);
        when(acc.owner()).thenReturn(mock(UserSummaryRecord.class));
        when(acc.den()).thenReturn("Acc. Details");
        when(acc.basedAccManifestId()).thenReturn(BASE_ACC_MANIFEST_ID);
        when(accQuery.getAccSummary(ACC_MANIFEST_ID)).thenReturn(acc);
        when(asccpQuery.getAsccpSummaryList(ACC_MANIFEST_ID)).thenReturn(List.of());
        when(repositoryFactory.accQueryRepository(any())).thenReturn(accQuery);
        when(repositoryFactory.accCommandRepository(any())).thenReturn(accCommand);
        when(repositoryFactory.asccpQueryRepository(any())).thenReturn(asccpQuery);
    }

    @Test
    void purgeIsRefusedWhenTheAccHasDerivedAccs() {
        when(accQuery.getInheritedAccSummaryList(ACC_MANIFEST_ID)).thenReturn(List.of(mock(AccSummaryRecord.class)));

        assertThrows(IllegalArgumentException.class, () -> service.purge(mock(ScoreUser.class), ACC_MANIFEST_ID));
        verify(accCommand, never()).delete(any(AccManifestId.class));
    }

    @Test
    void siblingsOfTheAccDoNotBlockThePurge() {
        when(accQuery.getInheritedAccSummaryList(BASE_ACC_MANIFEST_ID)).thenReturn(List.of(mock(AccSummaryRecord.class)));
        when(accQuery.getInheritedAccSummaryList(ACC_MANIFEST_ID)).thenReturn(List.of());
        when(accCommand.delete((AccManifestId) ACC_MANIFEST_ID)).thenReturn(true);

        assertTrue(service.purge(mock(ScoreUser.class), ACC_MANIFEST_ID));
    }
}
