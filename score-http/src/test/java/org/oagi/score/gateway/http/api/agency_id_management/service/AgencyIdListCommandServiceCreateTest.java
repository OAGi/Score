package org.oagi.score.gateway.http.api.agency_id_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.controller.payload.CreateAgencyIdListRequest;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListCommandRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** A developer has one Agency ID List in a release; an end user may have more. */
class AgencyIdListCommandServiceCreateTest {

    private final ReleaseId releaseId = new ReleaseId(BigInteger.ONE);
    private final AgencyIdListManifestId manifestId = new AgencyIdListManifestId(BigInteger.TEN);
    private final CreateAgencyIdListRequest request = new CreateAgencyIdListRequest(new LibraryId(BigInteger.ONE), releaseId, null);
    private final ScoreUser developer = new ScoreUser(new UserId(BigInteger.TEN), "dev", "Dev",
            "dev@example.com", true, List.of(ScoreRole.DEVELOPER));
    private final ScoreUser endUser = new ScoreUser(new UserId(BigInteger.valueOf(11)), "user", "User",
            "user@example.com", false, List.of(ScoreRole.END_USER));

    private AgencyIdListCommandService service;
    private AgencyIdListCommandRepository command;
    private AgencyIdListQueryRepository query;
    private ReleaseQueryRepository releaseQuery;

    @BeforeEach
    void setUp() {
        service = new AgencyIdListCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        command = mock(AgencyIdListCommandRepository.class);
        query = mock(AgencyIdListQueryRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        when(repositoryFactory.agencyIdListCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.agencyIdListQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.logCommandRepository(any())).thenReturn(mock(LogCommandRepository.class));
        when(command.create(any(), any())).thenReturn(manifestId);
        releaseQuery = mock(ReleaseQueryRepository.class);
        when(repositoryFactory.releaseQueryRepository(any())).thenReturn(releaseQuery);
        releaseIsWorking(true);
    }

    private void releaseIsWorking(boolean working) {
        ReleaseSummaryRecord release = mock(ReleaseSummaryRecord.class);
        when(release.isWorkingRelease()).thenReturn(working);
        when(releaseQuery.getReleaseSummary(releaseId)).thenReturn(release);
    }

    @Test
    void a_developer_cannot_create_a_second_agency_id_list_in_a_release() {
        when(query.countActiveAgencyIdLists(releaseId)).thenReturn(1);

        assertThrows(IllegalArgumentException.class, () -> service.create(developer, request));
        verify(command, never()).create(any(), any());
    }

    @Test
    void a_developer_creates_the_first_agency_id_list_of_a_release() {
        when(query.countActiveAgencyIdLists(releaseId)).thenReturn(0);

        assertEquals(manifestId, service.create(developer, request));
    }

    @Test
    void a_developer_cannot_create_one_outside_the_working_release() {
        releaseIsWorking(false);

        assertThrows(IllegalArgumentException.class, () -> service.create(developer, request));
        verify(command, never()).create(any(), any());
    }

    @Test
    void an_end_user_cannot_create_one_in_the_working_release() {
        assertThrows(IllegalArgumentException.class, () -> service.create(endUser, request));
        verify(command, never()).create(any(), any());
    }

    @Test
    void the_release_of_the_list_it_is_based_on_is_the_release_that_is_checked() {
        AgencyIdListManifestId based = new AgencyIdListManifestId(BigInteger.valueOf(5));
        when(query.getAgencyIdListReleaseId(based)).thenReturn(releaseId);

        assertThrows(IllegalArgumentException.class,
                () -> service.create(endUser, new CreateAgencyIdListRequest(new LibraryId(BigInteger.ONE), new ReleaseId(BigInteger.TWO), based)));
        verify(command, never()).create(any(), any());
    }

    @Test
    void an_end_user_may_create_more_than_one() {
        releaseIsWorking(false);
        when(query.countActiveAgencyIdLists(releaseId)).thenReturn(1);

        assertEquals(manifestId, service.create(endUser, request));
    }
}
