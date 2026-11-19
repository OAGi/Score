package org.oagi.score.gateway.http.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** What a user who may not call a method is told: what is lacking, or what the method says. */
class AccessControlExceptionTest {

    private static final ScoreUser USER = new ScoreUser(null, "someone", "Someone", "s@example.com", true, java.util.List.of(ScoreRole.END_USER));

    @Test
    void theRoleThatIsRequiredIsWhatIsLacking() {
        assertEquals("Only an administrator can access this.",
                new AccessControlException(USER, new ScoreRole[]{ScoreRole.ADMINISTRATOR}, "").getMessage());
        assertEquals("Only a developer can access this.",
                new AccessControlException(USER, new ScoreRole[]{ScoreRole.DEVELOPER}, null).getMessage());
    }

    @Test
    void severalRolesAreToldAsOneOfThem() {
        assertEquals("Only an administrator or a developer can access this.",
                new AccessControlException(USER, new ScoreRole[]{ScoreRole.ADMINISTRATOR, ScoreRole.DEVELOPER}, "").getMessage());
        assertEquals("Only an administrator, a developer or an end user can access this.",
                new AccessControlException(USER, new ScoreRole[]{ScoreRole.ADMINISTRATOR, ScoreRole.DEVELOPER, ScoreRole.END_USER}, " ").getMessage());
    }

    @Test
    void whatTheMethodSaysIsToldInsteadAndTheRolesAreKept() {
        AccessControlException e = new AccessControlException(USER, new ScoreRole[]{ScoreRole.ADMINISTRATOR}, "Only an administrator loads release packages.");

        assertEquals("Only an administrator loads release packages.", e.getMessage());
        assertEquals(java.util.List.of(ScoreRole.ADMINISTRATOR), e.requiredAnyRole());
    }
}
