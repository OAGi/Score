package org.oagi.score.gateway.http.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link AccessPrivilege#toAccessPrivilege}.
 *
 * <p>Pins the Issue #1312 change: a non-owner of a WIP BIE resolves to {@link AccessPrivilege#CanView}
 * (read-only), like QA/Production — while the owner still gets {@link AccessPrivilege#CanEdit}. The
 * separate {@link CcState} overload (core components) is deliberately unchanged and is pinned here too.</p>
 */
class AccessPrivilegeTest {

    private static final UserId OWNER = new UserId(BigInteger.valueOf(1));
    private static final UserId OTHER = new UserId(BigInteger.valueOf(2));

    private static ScoreUser user(UserId userId, ScoreRole... roles) {
        return new ScoreUser(userId, "u" + userId.value(), "name", "e@x.com", true, List.of(roles));
    }

    @Test
    @DisplayName("WIP: the owner can edit")
    void wipOwner_canEdit() {
        assertEquals(AccessPrivilege.CanEdit,
                AccessPrivilege.toAccessPrivilege(user(OWNER, ScoreRole.DEVELOPER), OWNER, BieState.WIP));
    }

    @Test
    @DisplayName("Issue #1312 — WIP: a non-owner may view read-only (was Prohibited)")
    void wipNonOwner_canView() {
        assertEquals(AccessPrivilege.CanView,
                AccessPrivilege.toAccessPrivilege(user(OTHER, ScoreRole.DEVELOPER), OWNER, BieState.WIP));
    }

    @Test
    @DisplayName("Issue #1312 — WIP: an end-user non-owner may also view read-only")
    void wipEndUserNonOwner_canView() {
        assertEquals(AccessPrivilege.CanView,
                AccessPrivilege.toAccessPrivilege(user(OTHER, ScoreRole.END_USER), OWNER, BieState.WIP));
    }

    @Test
    @DisplayName("QA: the owner can move; a non-owner can view")
    void qa_ownerMoves_nonOwnerViews() {
        assertEquals(AccessPrivilege.CanMove,
                AccessPrivilege.toAccessPrivilege(user(OWNER, ScoreRole.DEVELOPER), OWNER, BieState.QA));
        assertEquals(AccessPrivilege.CanView,
                AccessPrivilege.toAccessPrivilege(user(OTHER, ScoreRole.DEVELOPER), OWNER, BieState.QA));
    }

    @Test
    @DisplayName("Production is view-only for everyone")
    void production_canView() {
        assertEquals(AccessPrivilege.CanView,
                AccessPrivilege.toAccessPrivilege(user(OWNER, ScoreRole.DEVELOPER), OWNER, BieState.Production));
        assertEquals(AccessPrivilege.CanView,
                AccessPrivilege.toAccessPrivilege(user(OTHER, ScoreRole.DEVELOPER), OWNER, BieState.Production));
    }

    @Test
    @DisplayName("Initiating is Unprepared")
    void initiating_unprepared() {
        assertEquals(AccessPrivilege.Unprepared,
                AccessPrivilege.toAccessPrivilege(user(OWNER, ScoreRole.DEVELOPER), OWNER, BieState.Initiating));
    }

    @Test
    @DisplayName("Guard: the CcState overload (core components) still forbids a non-owner on WIP")
    void ccStateOverload_wipNonOwner_stillProhibited() {
        assertEquals(AccessPrivilege.Prohibited,
                AccessPrivilege.toAccessPrivilege(
                        user(OTHER, ScoreRole.DEVELOPER), OWNER, CcState.WIP, /* isWorkingRelease */ true));
        assertEquals(AccessPrivilege.CanEdit,
                AccessPrivilege.toAccessPrivilege(
                        user(OWNER, ScoreRole.DEVELOPER), OWNER, CcState.WIP, /* isWorkingRelease */ true));
    }
}
