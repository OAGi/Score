package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * pressing Ctrl/Cmd+S on the BIE Package detail page bypassed the Update button's guard
 * and saved the package with empty required fields. {@code update()} only checked ownership/WIP state,
 * not that name/versionId/versionName are non-empty, so the empty values were persisted.
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
    void update_persists_when_the_description_is_empty_or_null() {
        when(command.update(any(), any(), any(), any(), any(), any())).thenReturn(true);

        service.update(requester, aRequest("Name", "v1.0", "v1", ""));
        service.update(requester, aRequest("Name", "v1.0", "v1", null));

        verify(command).update(PACKAGE_ID, "Name", "v1.0", "v1", "", null);
        verify(command).update(PACKAGE_ID, "Name", "v1.0", "v1", null, null);
    }

    @Test
    void update_persists_when_all_required_fields_are_non_empty() {
        when(command.update(any(), any(), any(), any(), any(), any())).thenReturn(true);

        service.update(requester, aRequest("Name", "v1.0", "v1", "desc"));

        verify(command).update(PACKAGE_ID, "Name", "v1.0", "v1", "desc", null);
    }

    private ScoreUser anotherUser(ScoreRole role) {
        return new ScoreUser(new UserId(BigInteger.valueOf(99)), "other", "Other",
                "other@example.com", true, List.of(role));
    }

    private UpdateBiePackageRequest aStateRequest(BieState state) {
        return new UpdateBiePackageRequest(PACKAGE_ID, null, null, null, null, null, state);
    }

    @Test
    void a_non_owner_who_is_not_an_administrator_cannot_update_or_change_the_state() {
        ScoreUser other = anotherUser(ScoreRole.DEVELOPER);

        assertThrows(DataAccessForbiddenException.class,
                () -> service.update(other, aRequest("Name", "v1.0", "v1", "desc")));
        assertThrows(DataAccessForbiddenException.class,
                () -> service.update(other, aStateRequest(BieState.QA)));
        verify(command, never()).update(any(), any(), any(), any(), any(), any());
        verify(command, never()).updateState(any(), any());
    }

    @Test
    void an_administrator_who_is_not_the_owner_can_update_the_package() {
        when(command.update(any(), any(), any(), any(), any(), any())).thenReturn(true);

        assertTrue(service.update(anotherUser(ScoreRole.ADMINISTRATOR), aRequest("Name", "v1.0", "v1", "desc")));

        verify(command).update(PACKAGE_ID, "Name", "v1.0", "v1", "desc", null);
    }

    @Test
    void an_administrator_who_is_not_the_owner_can_change_the_state() {
        when(command.updateState(any(), any())).thenReturn(true);

        assertTrue(service.update(anotherUser(ScoreRole.ADMINISTRATOR), aStateRequest(BieState.QA)));

        verify(command).updateState(PACKAGE_ID, BieState.QA);
    }

    @Test
    void an_administrator_who_is_not_the_owner_can_add_and_delete_BIEs() {
        ScoreUser admin = anotherUser(ScoreRole.ADMINISTRATOR);

        service.addBieToBiePackage(admin, PACKAGE_ID, List.of());
        service.deleteBieInBiePackage(admin, PACKAGE_ID, List.of());

        verify(command).addBieToBiePackage(PACKAGE_ID, List.of());
        verify(command).deleteBieInBiePackage(PACKAGE_ID, List.of());
    }

    @Test
    void an_administrator_still_cannot_update_a_package_that_is_not_WIP() {
        BiePackageDetailsRecord qa = aWipBiePackageOwnedByRequester();
        when(query.getBiePackageDetails(PACKAGE_ID)).thenReturn(new BiePackageDetailsRecord(
                PACKAGE_ID, null, null, "Name", "v1.0", "v1", "desc",
                null, null, BieState.QA, null, null, null, qa.owner(), null, null));

        assertThrows(DataAccessForbiddenException.class,
                () -> service.update(anotherUser(ScoreRole.ADMINISTRATOR), aRequest("Name", "v1.0", "v1", "desc")));
    }
}
