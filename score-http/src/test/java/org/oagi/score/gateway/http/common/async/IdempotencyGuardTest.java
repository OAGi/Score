package org.oagi.score.gateway.http.common.async;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdempotencyGuardTest {

    enum State {Drafting, Draft}

    @Test
    void theFirstHandlingFindsTheStateTheWorkMovesOn() {
        assertTrue(IdempotencyGuard.stateIs(() -> Optional.of(State.Drafting), State.Drafting).isFirstHandling());
    }

    @Test
    void aHandlingThatFindsTheWorkDoneIsNotTheFirst() {
        assertFalse(IdempotencyGuard.stateIs(() -> Optional.of(State.Draft), State.Drafting).isFirstHandling());
    }

    @Test
    void whatIsNotThereAnyMoreIsNotTheFirstHandlingEither() {
        assertFalse(IdempotencyGuard.stateIs(Optional::<State>empty, State.Drafting).isFirstHandling());
    }
}
