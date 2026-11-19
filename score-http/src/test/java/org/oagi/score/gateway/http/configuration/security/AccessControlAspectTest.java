package org.oagi.score.gateway.http.configuration.security;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccessControlAspectTest {

    interface Things {
        String open();

        String forAdministrators();

        String forDevelopersOrEndUsers();

        String withoutTheCheck();
    }

    static class JooqThings extends JooqBaseRepository implements Things {
        JooqThings(ScoreUser requester) {
            super((DSLContext) null, requester, null);
        }

        @Override
        @AccessControl
        public String open() {
            return "open";
        }

        @Override
        @AccessControl(requiredAnyRole = {ScoreRole.ADMINISTRATOR})
        public String forAdministrators() {
            return "administrators";
        }

        @Override
        @AccessControl(requiredAnyRole = {ScoreRole.DEVELOPER, ScoreRole.END_USER})
        public String forDevelopersOrEndUsers() {
            return "developers or end users";
        }

        @Override
        @AccessControl(ignore = true, requiredAnyRole = {ScoreRole.ADMINISTRATOR})
        public String withoutTheCheck() {
            return "without";
        }
    }

    private static ScoreUser userOf(ScoreRole... roles) {
        return new ScoreUser(new UserId(BigInteger.ONE), "u", "u", null, false, List.of(roles));
    }

    private static Things guarded(ScoreUser requester) {
        return new AccessControlAspect().guard(new JooqThings(requester), Things.class);
    }

    @Test
    void aUserWithOneOfTheRolesIsLetThrough() {
        Things endUser = guarded(userOf(ScoreRole.END_USER));

        assertEquals("developers or end users", endUser.forDevelopersOrEndUsers());
        assertEquals("open", endUser.open());
        assertEquals("administrators", guarded(userOf(ScoreRole.END_USER, ScoreRole.ADMINISTRATOR)).forAdministrators());
    }

    @Test
    void aUserWithNoneOfTheRolesIsRefused() {
        Things endUser = guarded(userOf(ScoreRole.END_USER));

        assertThrows(AccessControlException.class, endUser::forAdministrators);
    }

    @Test
    void aCallWithNoUserIsRefusedAndIgnoreLetsAnyoneThrough() {
        Things nobody = guarded(null);

        assertThrows(AccessControlException.class, nobody::open);
        assertThrows(AccessControlException.class, nobody::forDevelopersOrEndUsers);
        assertEquals("without", nobody.withoutTheCheck());
    }

    interface Plain {
        String name();
    }

    @Test
    void aRepositoryWithNothingMarkedIsGivenBackAsItIs() {
        Plain plain = new Plain() {
            @Override
            public String name() {
                return "plain";
            }
        };

        assertSame(plain, new AccessControlAspect().guard(plain, Plain.class));
    }

    /** A bean of Spring, not a repository: the user is the one among the arguments of the call. */
    public static class SomeService {
        @AccessControl(requiredAnyRole = {ScoreRole.ADMINISTRATOR})
        public String forAdministrators(ScoreUser requester) {
            return "administrators";
        }

        @AccessControl
        public String forAnyone(String nobody) {
            return "anyone";
        }
    }

    @Test
    void aBeanOfSpringIsCheckedByTheUserAmongTheArgumentsOfTheCall() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class))
                .withBean(AccessControlAspect.class)
                .withBean(SomeService.class)
                .run(context -> {
                    SomeService service = context.getBean(SomeService.class);

                    assertTrue(AopUtils.isAopProxy(service));
                    assertEquals("administrators", service.forAdministrators(userOf(ScoreRole.END_USER, ScoreRole.ADMINISTRATOR)));
                    assertThrows(AccessControlException.class, () -> service.forAdministrators(userOf(ScoreRole.END_USER)));
                    assertThrows(AccessControlException.class, () -> service.forAdministrators(null));
                    assertThrows(AccessControlException.class, () -> service.forAnyone("no user among the arguments"));
                });
    }
}
