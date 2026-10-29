package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpdateBiePackageRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * pressing Ctrl/Cmd+S on the BIE Package detail page bypassed the Update button's guard
 * and saved the package with empty required fields. {@code update()} only checked ownership/WIP state,
 * not that name/versionId/versionName/description are non-empty, so the empty values were persisted.
 */
class BiePackageCommandServiceTest {

    private BiePackageCommandService service;
    private RepositoryFactory repositoryFactory;
    private BiePackageCommandRepository command;
    private BiePackageQueryRepository query;

    private static final BiePackageId PACKAGE_ID = new BiePackageId(BigInteger.valueOf(5));

    private ScoreUser requester;

    @BeforeEach
    void setUp() {
        service = new BiePackageCommandService();
        repositoryFactory = mock(RepositoryFactory.class);
        command = mock(BiePackageCommandRepository.class);
        query = mock(BiePackageQueryRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
                "tester@example.com", true, List.of(ScoreRole.DEVELOPER));

        when(repositoryFactory.biePackageCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.biePackageQueryRepository(any())).thenReturn(query);
        when(query.getBiePackageDetails(PACKAGE_ID)).thenReturn(aWipBiePackageOwnedByRequester());
    }

    private BiePackageDetailsRecord aWipBiePackageOwnedByRequester() {
        return new BiePackageDetailsRecord(PACKAGE_ID, null, null, "Name", "v1.0", "v1", "desc",
                null, null, BieState.WIP, null, null, null,
                new UserSummaryRecord(requester.userId(), "tester", "Tester", List.of()),
                null, null);
    }

    private UpdateBiePackageRequest aRequest(String name, String versionId, String versionName, String description) {
        return new UpdateBiePackageRequest(PACKAGE_ID, name, versionId, versionName, description, null, null);
    }

    @Test
    void update_rejects_an_empty_name() {
        assertThrows(IllegalArgumentException.class,
                () -> service.update(requester, aRequest("", "v1.0", "v1", "desc")));
        verify(command, never()).update(any(), any(), any(), any(), any(), any());
    }

    @Test
    void update_rejects_an_empty_versionId() {
        assertThrows(IllegalArgumentException.class,
                () -> service.update(requester, aRequest("Name", "", "v1", "desc")));
        verify(command, never()).update(any(), any(), any(), any(), any(), any());
    }

    @Test
    void update_rejects_an_empty_versionName() {
        assertThrows(IllegalArgumentException.class,
                () -> service.update(requester, aRequest("Name", "v1.0", "", "desc")));
        verify(command, never()).update(any(), any(), any(), any(), any(), any());
    }

    @Test
    void update_rejects_an_empty_description() {
        assertThrows(IllegalArgumentException.class,
                () -> service.update(requester, aRequest("Name", "v1.0", "v1", "")));
        verify(command, never()).update(any(), any(), any(), any(), any(), any());
    }

    @Test
    void update_persists_when_all_required_fields_are_non_empty() {
        when(command.update(any(), any(), any(), any(), any(), any())).thenReturn(true);

        service.update(requester, aRequest("Name", "v1.0", "v1", "desc"));

        verify(command).update(PACKAGE_ID, "Name", "v1.0", "v1", "desc", null);
    }
}
