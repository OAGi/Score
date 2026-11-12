package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BieStateTransitionServiceRootTransitionTest {

    @ParameterizedTest
    @CsvSource({
            "WIP, Production",
            "WIP, Initiating",
            "QA, Initiating",
            "Initiating, QA",
            "Initiating, Production",
            "Initiating, WIP",
            "Production, WIP",
            "Production, QA"
    })
    void rejects_a_transition_that_is_not_legal(BieState current, BieState requested) {
        assertThrows(IllegalArgumentException.class,
                () -> BieStateTransitionService.ensureLegalRootTransition(current, requested));
    }

    @ParameterizedTest
    @CsvSource({
            "WIP, QA",
            "QA, WIP",
            "QA, Production",
            "WIP, WIP",
            "QA, QA",
            "Production, Production"
    })
    void accepts_a_legal_transition_and_the_state_the_bie_already_has(BieState current, BieState requested) {
        assertDoesNotThrow(() -> BieStateTransitionService.ensureLegalRootTransition(current, requested));
    }

    @Test
    void never_accepts_initiating_as_a_target_even_when_it_is_the_current_state() {
        assertThrows(IllegalArgumentException.class,
                () -> BieStateTransitionService.ensureLegalRootTransition(BieState.Initiating, BieState.Initiating));
    }

    @Test
    void validateStateChange_rejects_wip_to_production_before_anything_else() {
        var id = new org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId(BigInteger.ONE);
        RepositoryFactory factory = mock(RepositoryFactory.class);
        TopLevelAsbiepQueryRepository query = mock(TopLevelAsbiepQueryRepository.class);
        TopLevelAsbiepSummaryRecord summary = mock(TopLevelAsbiepSummaryRecord.class);
        when(summary.state()).thenReturn(BieState.WIP);
        when(query.getTopLevelAsbiepSummary(id)).thenReturn(summary);
        ScoreUser requester = mock(ScoreUser.class);
        when(factory.topLevelAsbiepQueryRepository(requester)).thenReturn(query);
        BieStateTransitionService service = new BieStateTransitionService();
        ReflectionTestUtils.setField(service, "repositoryFactory", factory);

        assertThrows(IllegalArgumentException.class,
                () -> service.validateStateChange(requester, id, BieState.Production, List.of()));
    }
}
