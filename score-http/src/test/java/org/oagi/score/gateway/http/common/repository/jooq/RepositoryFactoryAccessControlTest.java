package org.oagi.score.gateway.http.common.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.AccessControlAspect;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** What is marked with {@link AccessControl} is checked only when the factory gives the repository the check; this keeps one from being left out. */
class RepositoryFactoryAccessControlTest {

    interface Things {
        String open();
    }

    /** A repository with a method that only an administrator may call, as the factory below makes it. */
    static class JooqThings extends JooqBaseRepository implements Things {
        JooqThings(ScoreUser requester, RepositoryFactory factory) {
            super(DSL.using(SQLDialect.MARIADB), requester, factory);
        }

        @Override
        @AccessControl(requiredAnyRole = {ScoreRole.ADMINISTRATOR})
        public String open() {
            return "open";
        }
    }

    public static class ThingsFactory extends RepositoryFactory {
        public ThingsFactory() {
            super(DSL.using(SQLDialect.MARIADB));
        }

        public Things things(ScoreUser requester) {
            return new JooqThings(requester, this);
        }
    }

    private final ThingsFactory factory = (ThingsFactory) new AccessControlAspect().guard(new ThingsFactory());

    private static ScoreUser userOf(ScoreRole... roles) {
        return new ScoreUser(new UserId(BigInteger.ONE), "u", "u", null, false, List.of(roles));
    }

    @Test
    void aRepositoryWithAMarkedMethodIsCheckedWhateverItIsAskedForBy() {
        assertThrows(AccessControlException.class, () -> factory.things(null).open());
        assertThrows(AccessControlException.class, () -> factory.things(userOf(ScoreRole.END_USER)).open());
        assertEquals("open", factory.things(userOf(ScoreRole.END_USER, ScoreRole.ADMINISTRATOR)).open());
    }

    @Test
    void aRepositoryWithNoMarkedMethodIsGivenAsItIs() {
        assertFalse(Proxy.isProxyClass(factory.contextCategoryQueryRepository(userOf(ScoreRole.END_USER)).getClass()));
    }

    @Test
    void theFactoryThatSpringMakesIsWrappedAndChecksWhatItMakes() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class))
                .withBean(DSLContext.class, () -> DSL.using(SQLDialect.MARIADB))
                .withBean(AccessControlAspect.class)
                .withBean(ThingsFactory.class)
                .run(context -> {
                    ThingsFactory bean = context.getBean(ThingsFactory.class);

                    assertTrue(AopUtils.isAopProxy(bean));
                    assertThrows(AccessControlException.class, () -> bean.things(userOf(ScoreRole.END_USER)).open());
                });
    }

    @Test
    void aRepositoryAsksTheWrappedFactoryForTheOthersSoThoseAreCheckedToo() {
        JooqBaseRepository repository = (JooqBaseRepository) factory.contextCategoryQueryRepository(userOf(ScoreRole.END_USER));

        assertTrue(AopUtils.isAopProxy(repository.repositoryFactory()));
    }

    @Test
    void everyMarkedMethodIsOneOfTheInterfaceItsRepositoryIsCalledBy() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(JooqBaseRepository.class));
        List<String> unreachable = new ArrayList<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("org.oagi.score.gateway.http")) {
            Class<?> type = classOf(definition);
            for (Method method : type.getMethods()) {
                if (!method.isAnnotationPresent(AccessControl.class)) {
                    continue;
                }
                boolean declaredByInterface = Arrays.stream(type.getInterfaces()).anyMatch(i -> declares(i, method));
                if (!declaredByInterface) {
                    unreachable.add(type.getSimpleName() + "." + method.getName());
                }
            }
        }
        assertEquals(List.of(), unreachable, "These are marked but are not called by an interface, so the check cannot reach them");
    }

    private static boolean declares(Class<?> type, Method method) {
        try {
            type.getMethod(method.getName(), method.getParameterTypes());
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static Class<?> classOf(BeanDefinition definition) {
        try {
            return Class.forName(definition.getBeanClassName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
