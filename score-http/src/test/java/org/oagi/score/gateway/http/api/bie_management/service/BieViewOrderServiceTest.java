package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.BieViewOrderEntry;
import org.oagi.score.gateway.http.api.bie_management.repository.BieViewOrderCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.BieViewOrderQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BieViewOrderServiceTest {

    private ScoreUser user(ScoreRole... roles) {
        return new ScoreUser(new UserId(BigInteger.ONE), "u", "U", null, false, List.of(roles));
    }

    @Test
    void updateViewOrderRejectsNonDeveloper() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        BieViewOrderService service = new BieViewOrderService(repositoryFactory);
        ScoreUser endUser = user(ScoreRole.END_USER);
        AccManifestId fromAcc = new AccManifestId(BigInteger.TEN);

        assertThatThrownBy(() -> service.updateViewOrder(endUser, fromAcc, List.of()))
                .isInstanceOf(AccessDeniedException.class);

        // The developer gate must fail before any repository is touched.
        verifyNoInteractions(repositoryFactory);
    }

    @Test
    void updateViewOrderUpsertsForDeveloperWithNoStateGate() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        BieViewOrderCommandRepository command = mock(BieViewOrderCommandRepository.class);
        ScoreUser developer = user(ScoreRole.DEVELOPER);
        when(repositoryFactory.bieViewOrderCommandRepository(developer)).thenReturn(command);

        BieViewOrderService service = new BieViewOrderService(repositoryFactory);
        AccManifestId fromAcc = new AccManifestId(BigInteger.TEN);
        List<BieViewOrderEntry> entries = List.of(
                new BieViewOrderEntry(fromAcc, null, null, 100));

        // No CcState is consulted by design — the developer write succeeds unconditionally.
        service.updateViewOrder(developer, fromAcc, entries);

        verify(command).upsert(fromAcc, entries);
    }

    @Test
    void resetViewOrderRejectsNonDeveloper() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        BieViewOrderService service = new BieViewOrderService(repositoryFactory);
        ScoreUser endUser = user(ScoreRole.END_USER);
        AccManifestId fromAcc = new AccManifestId(BigInteger.TEN);

        assertThatThrownBy(() -> service.resetViewOrder(endUser, fromAcc))
                .isInstanceOf(AccessDeniedException.class);

        // The developer gate must fail before any repository is touched.
        verifyNoInteractions(repositoryFactory);
    }

    @Test
    void resetViewOrderDeletesTheParentsRowsForDeveloperWithNoStateGate() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        BieViewOrderCommandRepository command = mock(BieViewOrderCommandRepository.class);
        ScoreUser developer = user(ScoreRole.DEVELOPER);
        when(repositoryFactory.bieViewOrderCommandRepository(developer)).thenReturn(command);

        BieViewOrderService service = new BieViewOrderService(repositoryFactory);
        AccManifestId fromAcc = new AccManifestId(BigInteger.TEN);

        // No CcState is consulted by design — the developer reset succeeds unconditionally and only
        // clears rows keyed by this one view-parent ACC (deeper nested ACCs are untouched).
        service.resetViewOrder(developer, fromAcc);

        verify(command).deleteByFromAccManifestId(fromAcc);
    }

    @Test
    void getViewOrderReturnsTheParentsEntriesForAnyViewer() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        BieViewOrderQueryRepository query = mock(BieViewOrderQueryRepository.class);
        ScoreUser reader = user(ScoreRole.END_USER);
        AccManifestId fromAcc = new AccManifestId(BigInteger.TEN);
        List<BieViewOrderEntry> entries = List.of(new BieViewOrderEntry(fromAcc, null, null, 100));
        when(repositoryFactory.bieViewOrderQueryRepository(reader)).thenReturn(query);
        when(query.findByFromAccManifestId(fromAcc)).thenReturn(entries);

        BieViewOrderService service = new BieViewOrderService(repositoryFactory);

        // Read is available to any viewer (no developer gate) and is keyed purely by the view-parent ACC.
        assertThat(service.getViewOrder(reader, fromAcc)).isEqualTo(entries);
        verify(query).findByFromAccManifestId(fromAcc);
    }
}
