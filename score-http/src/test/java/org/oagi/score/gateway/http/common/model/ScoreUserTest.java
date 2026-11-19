package org.oagi.score.gateway.http.common.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreUserTest {

    private static ScoreUser userOf(ScoreRole... roles) {
        return new ScoreUser(null, "u", "u", null, false, List.of(roles));
    }

    @Test
    void hasAnyRoleIsTrueWhenAnyOfTheRolesIsHeldNotOnlyTheFirst() {
        ScoreUser endUser = userOf(ScoreRole.END_USER);

        assertTrue(endUser.hasAnyRole(ScoreRole.DEVELOPER, ScoreRole.END_USER));
        assertTrue(endUser.hasAnyRole(ScoreRole.END_USER, ScoreRole.DEVELOPER));
    }

    @Test
    void hasAnyRoleIsFalseWhenNoneOfTheRolesIsHeld() {
        assertFalse(userOf(ScoreRole.END_USER).hasAnyRole(ScoreRole.DEVELOPER, ScoreRole.ADMINISTRATOR));
        assertFalse(userOf().hasAnyRole(ScoreRole.DEVELOPER));
    }
}
