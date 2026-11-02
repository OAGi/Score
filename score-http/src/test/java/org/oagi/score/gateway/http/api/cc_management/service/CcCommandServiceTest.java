package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.asccp.AsccpCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.AsccpCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.AsccpQueryRepository;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * #26 coverage for {@link CcCommandService#createAsccp}: a Deleted role-of ACC must be rejected
 * the same way an abstract one already is, instead of silently creating an ASCCP on top of it.
 */
class CcCommandServiceTest {

    private static final ReleaseId RELEASE_ID = new ReleaseId(BigInteger.ONE);
    private static final AccManifestId ROLE_OF_ACC_MANIFEST_ID = new AccManifestId(BigInteger.TEN);
    private static final ReleaseSummaryRecord RELEASE =
            new ReleaseSummaryRecord(RELEASE_ID, new LibraryId(BigInteger.ONE), "1", ReleaseState.Published);

    private CcCommandService service;
    private AccQueryRepository accQueryRepository;
    private ReleaseQueryRepository releaseQueryRepository;
    private AsccpCommandRepository asccpCommandRepository;

    private ScoreUser requester;

    @BeforeEach
    void setUp() {
        service = new CcCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        accQueryRepository = mock(AccQueryRepository.class);
        releaseQueryRepository = mock(ReleaseQueryRepository.class);
        asccpCommandRepository = mock(AsccpCommandRepository.class);
        AsccpQueryRepository asccpQueryRepository = mock(AsccpQueryRepository.class);
        LogCommandRepository logCommandRepository = mock(LogCommandRepository.class);

        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        requester = new ScoreUser(new UserId(BigInteger.ONE), "eu", "End User",
                "eu@example.com", true, List.of(ScoreRole.END_USER));

        when(repositoryFactory.accQueryRepository(any())).thenReturn(accQueryRepository);
        when(repositoryFactory.releaseQueryRepository(any())).thenReturn(releaseQueryRepository);
        when(repositoryFactory.asccpCommandRepository(any())).thenReturn(asccpCommandRepository);
        when(repositoryFactory.asccpQueryRepository(any())).thenReturn(asccpQueryRepository);
        when(repositoryFactory.logCommandRepository(any())).thenReturn(logCommandRepository);
        when(releaseQueryRepository.getReleaseSummary(RELEASE_ID)).thenReturn(RELEASE);
    }

    private static AccSummaryRecord roleOfAcc(CcState state, boolean isAbstract) {
        return new AccSummaryRecord(null, RELEASE, ROLE_OF_ACC_MANIFEST_ID, null, null, "Default", null,
                "Role. Details", "Role", null, OagisComponentType.Semantics, isAbstract, false, state,
                null, null, 1, null, null, null);
    }

    private static AsccpCreateRequest createRequest() {
        return AsccpCreateRequest.builder(RELEASE_ID, ROLE_OF_ACC_MANIFEST_ID).build();
    }

    @Test
    void createAsccp_rejectsDeletedRoleOfAccWith400() {
        when(accQueryRepository.getAccSummary(ROLE_OF_ACC_MANIFEST_ID)).thenReturn(roleOfAcc(CcState.Deleted, false));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createAsccp(requester, createRequest()));
        assertTrue(ex.getMessage().contains("deleted"));
        verify(asccpCommandRepository, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void createAsccp_rejectsAbstractRoleOfAccWith400() {
        when(accQueryRepository.getAccSummary(ROLE_OF_ACC_MANIFEST_ID)).thenReturn(roleOfAcc(CcState.WIP, true));

        assertThrows(IllegalArgumentException.class, () -> service.createAsccp(requester, createRequest()));
        verify(asccpCommandRepository, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void createAsccp_succeedsForUsableRoleOfAcc() {
        when(accQueryRepository.getAccSummary(ROLE_OF_ACC_MANIFEST_ID)).thenReturn(roleOfAcc(CcState.WIP, false));
        AsccpManifestId created = new AsccpManifestId(BigInteger.valueOf(99));
        when(asccpCommandRepository.create(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(created);

        AsccpManifestId result = service.createAsccp(requester, createRequest());

        assertEquals(created, result);
    }
}
