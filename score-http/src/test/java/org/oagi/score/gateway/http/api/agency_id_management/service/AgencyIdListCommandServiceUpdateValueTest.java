package org.oagi.score.gateway.http.api.agency_id_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.controller.payload.UpdateAgencyIdListRequest;
import org.oagi.score.gateway.http.api.agency_id_management.controller.payload.UpdateAgencyIdListValueRequest;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListDetailsRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueDetailsRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListCommandRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AgencyIdListCommandServiceUpdateValueTest {

    private final AgencyIdListManifestId manifestId = new AgencyIdListManifestId(BigInteger.ONE);
    private final AgencyIdListId agencyIdListId = new AgencyIdListId(BigInteger.ONE);
    private final ReleaseId releaseId = new ReleaseId(BigInteger.ONE);
    private final NamespaceId namespaceId = new NamespaceId(BigInteger.ONE);
    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester",
            "tester@example.com", false, List.of(ScoreRole.END_USER));

    private final AgencyIdListValueManifestId valueAId = new AgencyIdListValueManifestId(BigInteger.valueOf(101));
    private final AgencyIdListValueManifestId valueBId = new AgencyIdListValueManifestId(BigInteger.valueOf(102));

    private AgencyIdListCommandService service;
    private AgencyIdListCommandRepository command;
    private AgencyIdListQueryRepository query;

    @BeforeEach
    void setUp() {
        service = new AgencyIdListCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        command = mock(AgencyIdListCommandRepository.class);
        query = mock(AgencyIdListQueryRepository.class);
        LogCommandRepository logCommand = mock(LogCommandRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        when(repositoryFactory.agencyIdListCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.agencyIdListQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.logCommandRepository(any())).thenReturn(logCommand);

        AgencyIdListDetailsRecord details = mock(AgencyIdListDetailsRecord.class);
        when(details.state()).thenReturn(CcState.WIP);
        when(details.owner()).thenReturn(owner());
        when(details.agencyIdListId()).thenReturn(agencyIdListId);
        when(details.release()).thenReturn(new ReleaseSummaryRecord(releaseId, null, "2024-01", null));
        when(query.getAgencyIdListDetails(manifestId)).thenReturn(details);

        when(command.update(eq(manifestId), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);
    }

    @Test
    void update_clears_previous_default_when_a_different_value_becomes_the_default() {
        List<AgencyIdListValueDetailsRecord> existingValues = List.of(
                existingValue(valueAId), existingValue(valueBId));
        when(query.getAgencyIdListValueDetailsList(manifestId)).thenReturn(existingValues);

        UpdateAgencyIdListValueRequest valueA = new UpdateAgencyIdListValueRequest(
                valueAId, "A", "Name A", null, false, false, false);
        UpdateAgencyIdListValueRequest valueB = new UpdateAgencyIdListValueRequest(
                valueBId, "B", "Name B", null, false, true, false);

        service.update(requester, updateRequest(List.of(valueA, valueB)));

        // Value A was the previous default but is not requested as default anymore: it must be cleared.
        verify(command).updateValue(valueAId, "A", "Name A", null, false, false, false);
        verify(command).updateValue(valueBId, "B", "Name B", null, false, true, false);
    }

    @Test
    void update_rejects_a_value_or_meaning_without_text() {
        when(query.getAgencyIdListValueDetailsList(manifestId)).thenReturn(List.of());

        for (String blank : new String[]{null, "", "   "}) {
            var blankValue = new UpdateAgencyIdListValueRequest(null, blank, "Name", null, false, false, false);
            var blankName = new UpdateAgencyIdListValueRequest(null, "V", blank, null, false, false, false);
            assertThrows(IllegalArgumentException.class,
                    () -> service.update(requester, updateRequest(List.of(blankValue))));
            assertThrows(IllegalArgumentException.class,
                    () -> service.update(requester, updateRequest(List.of(blankName))));
        }

        verify(command, never()).createValue(any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean());
        verify(command, never()).update(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void update_rejects_a_name_list_id_or_version_without_text() {
        for (String blank : new String[]{null, "", "   "}) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.update(requester, updateRequest(blank, "1", "custom-list", List.of())));
            assertThrows(IllegalArgumentException.class,
                    () -> service.update(requester, updateRequest("Custom agency list", blank, "custom-list", List.of())));
            assertThrows(IllegalArgumentException.class,
                    () -> service.update(requester, updateRequest("Custom agency list", "1", blank, List.of())));
        }

        verify(command, never()).update(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void update_saves_the_trimmed_name_list_id_and_version() {
        when(query.getAgencyIdListValueDetailsList(manifestId)).thenReturn(List.of());

        service.update(requester, updateRequest(" Custom agency list ", " 1 ", " custom-list ", List.of()));

        verify(command).update(eq(manifestId), eq("Custom agency list"), eq("1"), eq("custom-list"),
                any(), any(), any(), any(), any());
    }

    @Test
    void update_applies_requested_flags_to_a_newly_added_value() {
        when(query.getAgencyIdListValueDetailsList(manifestId)).thenReturn(List.of());

        UpdateAgencyIdListValueRequest newValue = new UpdateAgencyIdListValueRequest(
                null, "C", "Name C", null, false, true, false);

        service.update(requester, updateRequest(List.of(newValue)));

        verify(command).createValue(manifestId, agencyIdListId, releaseId, "C", "Name C", null,
                false, true, false);
    }

    @Test
    void update_clears_previous_default_when_a_newly_added_value_becomes_the_default() {
        List<AgencyIdListValueDetailsRecord> existingValues = List.of(existingValue(valueAId));
        when(query.getAgencyIdListValueDetailsList(manifestId)).thenReturn(existingValues);

        UpdateAgencyIdListValueRequest valueA = new UpdateAgencyIdListValueRequest(
                valueAId, "A", "Name A", null, false, false, false);
        UpdateAgencyIdListValueRequest newValue = new UpdateAgencyIdListValueRequest(
                null, "C", "Name C", null, false, true, true);

        service.update(requester, updateRequest(List.of(valueA, newValue)));

        verify(command).updateValue(valueAId, "A", "Name A", null, false, false, false);
        verify(command).createValue(manifestId, agencyIdListId, releaseId, "C", "Name C", null,
                false, true, true);
    }

    @Test
    void update_rejects_a_newly_added_deprecated_value() {
        when(query.getAgencyIdListValueDetailsList(manifestId)).thenReturn(List.of());

        UpdateAgencyIdListValueRequest newValue = new UpdateAgencyIdListValueRequest(
                null, "C", "Name C", null, true, false, false);

        assertThrows(IllegalArgumentException.class,
                () -> service.update(requester, updateRequest(List.of(newValue))));
        verify(command, never()).createValue(any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void update_rejects_more_than_one_developer_default() {
        List<AgencyIdListValueDetailsRecord> existingValues = List.of(
                existingValue(valueAId), existingValue(valueBId));
        when(query.getAgencyIdListValueDetailsList(manifestId)).thenReturn(existingValues);

        UpdateAgencyIdListValueRequest valueA = new UpdateAgencyIdListValueRequest(
                valueAId, "A", "Name A", null, false, true, false);
        UpdateAgencyIdListValueRequest valueB = new UpdateAgencyIdListValueRequest(
                valueBId, "B", "Name B", null, false, true, false);

        assertThrows(IllegalArgumentException.class,
                () -> service.update(requester, updateRequest(List.of(valueA, valueB))));
        verify(command, never()).updateValue(any(), any(), any(), any(), any(), any(), any());
    }

    private UpdateAgencyIdListRequest updateRequest(List<UpdateAgencyIdListValueRequest> valueList) {
        return updateRequest("Custom agency list", "1", "custom-list", valueList);
    }

    private UpdateAgencyIdListRequest updateRequest(
            String name, String versionId, String listId, List<UpdateAgencyIdListValueRequest> valueList) {
        return new UpdateAgencyIdListRequest(manifestId,
                name, versionId, listId, null,
                null, null, namespaceId, false, valueList);
    }

    private AgencyIdListValueDetailsRecord existingValue(AgencyIdListValueManifestId valueManifestId) {
        AgencyIdListValueDetailsRecord value = mock(AgencyIdListValueDetailsRecord.class);
        when(value.agencyIdListValueManifestId()).thenReturn(valueManifestId);
        return value;
    }

    private UserSummaryRecord owner() {
        return new UserSummaryRecord(requester.userId(), "tester", "Tester", List.of(ScoreRole.END_USER));
    }
}
