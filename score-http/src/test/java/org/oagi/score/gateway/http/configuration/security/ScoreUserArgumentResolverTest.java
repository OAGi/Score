package org.oagi.score.gateway.http.configuration.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.WebConfiguration;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScoreUserArgumentResolverTest {

    public static class SomeController {
        @AccessControl(requiredAnyRole = {ScoreRole.ADMINISTRATOR})
        public ScoreUser forAdministrators(ScoreUser requester) {
            return requester;
        }

        public void other(String name, ScoreUser requester) {
        }
    }

    private final SessionService sessionService = mock(SessionService.class);

    @SuppressWarnings("unchecked")
    private final ObjectProvider<SessionService> provider = mock(ObjectProvider.class);

    private final ScoreUserArgumentResolver resolver = new ScoreUserArgumentResolver(provider);

    private static ScoreUser userOf(ScoreRole... roles) {
        return new ScoreUser(new UserId(BigInteger.ONE), "u", "u", null, false, List.of(roles));
    }

    private static MethodParameter parameter(String method, int index, Class<?>... types) throws Exception {
        Method m = SomeController.class.getMethod(method, types);
        return new MethodParameter(m, index);
    }

    private void signedInAs(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(provider.getObject()).thenReturn(sessionService);
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void itTakesAParameterOfTheTypeOfTheUserAndNoOther() throws Exception {
        assertTrue(resolver.supportsParameter(parameter("forAdministrators", 0, ScoreUser.class)));
        assertFalse(resolver.supportsParameter(parameter("other", 0, String.class, ScoreUser.class)));
        assertTrue(resolver.supportsParameter(parameter("other", 1, String.class, ScoreUser.class)));
    }

    @Test
    void theUserOfTheSignedInRequestIsReadOnce() throws Exception {
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated("u", "x", AuthorityUtils.createAuthorityList("end_user"));
        ScoreUser user = userOf(ScoreRole.END_USER);
        signedInAs(authentication);
        when(sessionService.asScoreUser(authentication)).thenReturn(user);

        assertSame(user, resolver.resolveArgument(parameter("forAdministrators", 0, ScoreUser.class), null, null, null));
        verify(sessionService, times(1)).asScoreUser(any(Authentication.class));
    }

    @Test
    void aRequestThatIsNotSignedInHasNoUser() throws Exception {
        assertNull(resolver.resolveArgument(parameter("forAdministrators", 0, ScoreUser.class), null, null, null));

        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        assertNull(resolver.resolveArgument(parameter("forAdministrators", 0, ScoreUser.class), null, null, null));
    }

    @Test
    void whatIsResolvedIsTheOneThatAccessControlChecksAndTheMethodGets() {
        ScoreUser administrator = userOf(ScoreRole.END_USER, ScoreRole.ADMINISTRATOR);
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class))
                .withBean(AccessControlAspect.class)
                .withBean(SomeController.class)
                .run(context -> {
                    SomeController controller = context.getBean(SomeController.class);

                    assertTrue(AopUtils.isAopProxy(controller));
                    assertSame(administrator, controller.forAdministrators(administrator));
                    assertThrows(AccessControlException.class, () -> controller.forAdministrators(userOf(ScoreRole.END_USER)));
                    assertThrows(AccessControlException.class, () -> controller.forAdministrators(null));
                });
    }

    @Test
    void theMvcConfigurationRegistersIt() {
        List<HandlerMethodArgumentResolver> resolvers = new java.util.ArrayList<>();

        new WebConfiguration(JsonMapper.builder().build(), resolver).addArgumentResolvers(resolvers);

        assertEquals(List.of(resolver), resolvers);
    }
}
