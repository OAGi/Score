package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.CopyBiePackageRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpdateBiePackageRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BieReplacementRecord;
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
    void update_rejects_a_whitespace_only_name_versionId_or_versionName() {
        assertThrows(IllegalArgumentException.class,
                () -> service.update(requester, aRequest("   ", "v1.0", "v1", "desc")));
        assertThrows(IllegalArgumentException.class,
                () -> service.update(requester, aRequest("Name", " 	", "v1", "desc")));
        assertThrows(IllegalArgumentException.class,
                () -> service.update(requester, aRequest("Name", "v1.0", "   ", "desc")));
        verify(command, never()).update(any(), any(), any(), any(), any(), any());
    }

    @Test
    void update_saves_the_trimmed_name_versionId_and_versionName() {
        when(command.update(any(), any(), any(), any(), any(), any())).thenReturn(true);

        service.update(requester, aRequest("  Name ", " v1.0", "v1 ", "desc"));

        verify(command).update(PACKAGE_ID, "Name", "v1.0", "v1", "desc", null);
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

    private void packageInState(BieState state) {
        BiePackageDetailsRecord wip = aWipBiePackageOwnedByRequester();
        when(query.getBiePackageDetails(PACKAGE_ID)).thenReturn(new BiePackageDetailsRecord(
                PACKAGE_ID, null, null, "Name", "v1.0", "v1", "desc",
                null, null, state, null, null, null, wip.owner(), null, null));
    }

    @Test
    void the_state_follows_only_the_moves_the_page_offers() {
        when(command.updateState(any(), any())).thenReturn(true);

        packageInState(BieState.WIP);
        service.update(requester, aStateRequest(BieState.QA));
        packageInState(BieState.QA);
        service.update(requester, aStateRequest(BieState.WIP));
        service.update(requester, aStateRequest(BieState.Production));

        verify(command).updateState(PACKAGE_ID, BieState.QA);
        verify(command).updateState(PACKAGE_ID, BieState.WIP);
        verify(command).updateState(PACKAGE_ID, BieState.Production);
    }

    @Test
    void a_production_package_cannot_leave_production_and_wip_cannot_skip_qa() {
        packageInState(BieState.Production);
        assertThrows(IllegalArgumentException.class, () -> service.update(requester, aStateRequest(BieState.WIP)));
        assertThrows(IllegalArgumentException.class, () -> service.update(requester, aStateRequest(BieState.QA)));

        packageInState(BieState.WIP);
        assertThrows(IllegalArgumentException.class, () -> service.update(requester, aStateRequest(BieState.Production)));
        assertThrows(IllegalArgumentException.class, () -> service.update(requester, aStateRequest(BieState.WIP)));
        verify(command, never()).updateState(any(), any());
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

    private static TopLevelAsbiepId tlaId(long value) {
        return new TopLevelAsbiepId(BigInteger.valueOf(value));
    }

    @Test
    void a_bie_that_was_replaced_cannot_replace_its_replacement() {
        // A is replaced by B (B.prev = A): replacing B with A would make a cycle
        when(query.getBieReplacementListInBiePackage(PACKAGE_ID))
                .thenReturn(List.of(new BieReplacementRecord(tlaId(2), tlaId(1))));

        assertThrows(IllegalArgumentException.class,
                () -> service.replaceBieInBiePackage(requester, PACKAGE_ID, tlaId(2), tlaId(1)));
        assertThrows(IllegalArgumentException.class,
                () -> service.replaceBieInBiePackage(requester, PACKAGE_ID, tlaId(1), tlaId(1)));
        verify(command, never()).replaceBieInBiePackage(any(), any(), any());
    }

    @Test
    void a_bie_outside_the_replacement_chain_can_replace() {
        when(query.getBieReplacementListInBiePackage(PACKAGE_ID))
                .thenReturn(List.of(new BieReplacementRecord(tlaId(2), tlaId(1))));

        service.replaceBieInBiePackage(requester, PACKAGE_ID, tlaId(2), tlaId(3));

        verify(command).replaceBieInBiePackage(PACKAGE_ID, tlaId(2), tlaId(3));
    }

    private BiePackageSummaryRecord aSummary(BiePackageId id, BieState state, BiePackageId prev) {
        return new BiePackageSummaryRecord(id, null, null, "Name", "v1.0", "v1", "desc",
                null, null, state, null, prev);
    }

    @Test
    void a_production_package_is_revised_once() {
        when(query.getBiePackageSummary(PACKAGE_ID)).thenReturn(aSummary(PACKAGE_ID, BieState.Production, null));
        when(query.getBiePackagesReferencingAsPrevious(List.of(PACKAGE_ID))).thenReturn(List.of());
        when(query.hasDuplicateVersion(any(), any())).thenReturn(false);

        service.revise(requester, PACKAGE_ID);

        verify(command).revise(eq(PACKAGE_ID), any());
    }

    @Test
    void a_package_that_already_has_a_newer_revision_cannot_be_revised_again() {
        BiePackageId revision = new BiePackageId(BigInteger.valueOf(6));
        when(query.getBiePackageSummary(PACKAGE_ID)).thenReturn(aSummary(PACKAGE_ID, BieState.Production, null));
        when(query.getBiePackagesReferencingAsPrevious(List.of(PACKAGE_ID)))
                .thenReturn(List.of(aSummary(revision, BieState.WIP, PACKAGE_ID)));

        assertThrows(IllegalArgumentException.class, () -> service.revise(requester, PACKAGE_ID));
        verify(command, never()).revise(any(), any());
    }

    @Test
    void a_revision_chain_can_keep_growing_from_its_latest_production_package() {
        BiePackageId second = new BiePackageId(BigInteger.valueOf(6));
        BiePackageId third = new BiePackageId(BigInteger.valueOf(7));
        when(query.hasDuplicateVersion(any(), any())).thenReturn(false);

        // PACKAGE_ID (Production) -> second (WIP, then Production): the first is closed, the latest is open.
        when(query.getBiePackageSummary(PACKAGE_ID)).thenReturn(aSummary(PACKAGE_ID, BieState.Production, null));
        when(query.getBiePackagesReferencingAsPrevious(List.of(PACKAGE_ID)))
                .thenReturn(List.of(aSummary(second, BieState.Production, PACKAGE_ID)));
        when(query.getBiePackageSummary(second)).thenReturn(aSummary(second, BieState.Production, PACKAGE_ID));
        when(query.getBiePackagesReferencingAsPrevious(List.of(second))).thenReturn(List.of());
        when(command.revise(eq(second), any())).thenReturn(third);

        assertThrows(IllegalArgumentException.class, () -> service.revise(requester, PACKAGE_ID));
        service.revise(requester, second);

        verify(command, never()).revise(eq(PACKAGE_ID), any());
        verify(command).revise(eq(second), any());
    }

    private BiePackageSummaryRecord aSummary(BieState state, UserId ownerId) {
        return new BiePackageSummaryRecord(PACKAGE_ID, null, null, "Name", "v1.0", "v1", "desc", null, null,
                state, new UserSummaryRecord(ownerId, "someone", "Someone", List.of()), null);
    }

    private static final UserId ANOTHER_USER = new UserId(BigInteger.valueOf(43));

    @Test
    void copy_rejects_an_unknown_package() {
        when(query.getBiePackageSummary(PACKAGE_ID)).thenReturn(null);
        assertThrows(org.oagi.score.gateway.http.common.model.NotFoundException.class,
                () -> service.copy(requester, new CopyBiePackageRequest(List.of(PACKAGE_ID))));
        verify(command, never()).copy(any());
    }

    @Test
    void copy_rejects_the_wip_package_of_another_user() {
        when(query.getBiePackageSummary(PACKAGE_ID)).thenReturn(aSummary(BieState.WIP, ANOTHER_USER));
        assertThrows(DataAccessForbiddenException.class, () -> service.copy(requester, new CopyBiePackageRequest(List.of(PACKAGE_ID))));
        verify(command, never()).copy(any());
    }

    @Test
    void copy_allows_qa_and_production_of_another_user() {
        for (BieState state : List.of(BieState.QA, BieState.Production)) {
            when(query.getBiePackageSummary(PACKAGE_ID)).thenReturn(aSummary(state, ANOTHER_USER));
            service.copy(requester, new CopyBiePackageRequest(List.of(PACKAGE_ID)));
        }
        verify(command, times(2)).copy(PACKAGE_ID);
    }

    @Test
    void copy_allows_the_own_wip_package() {
        when(query.getBiePackageSummary(PACKAGE_ID)).thenReturn(aSummary(BieState.WIP, requester.userId()));
        service.copy(requester, new CopyBiePackageRequest(List.of(PACKAGE_ID)));
        verify(command).copy(PACKAGE_ID);
    }
}
