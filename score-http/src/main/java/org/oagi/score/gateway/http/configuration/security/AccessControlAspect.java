package org.oagi.score.gateway.http.configuration.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps a call of a method marked with {@link AccessControl} from going further when the user it is made for may not make it:
 * there is no user, or the method names the roles that may call it ({@code requiredAnyRole}) and the user has none of them.
 *
 * <p>The user is the one a repository is made for; of a bean that is not a repository, it is the first {@link ScoreUser}
 * among the arguments of the call.</p>
 *
 * <p>The repositories are not made by Spring but by {@link RepositoryFactory}, so Spring does not wrap them by itself. It wraps the
 * factory (a bean), and what the factory makes is given the check here: a repository with a marked method is called by its
 * interface through a proxy, any other is given back as it is, which costs nothing (they are made for every request). A repository
 * asks the factory it was made by for the repositories it needs; it is told to ask the wrapped one, so those are checked too.</p>
 */
@Aspect
@Component
public class AccessControlAspect {

    private final Map<Class<?>, Boolean> marked = new ConcurrentHashMap<>();

    /**
     * Wraps a factory that Spring did not make (one a test makes), so that what it makes is checked as it is in the application.
     *
     * <p>Like {@link #guardRepositories}, this is here because the repositories are not Spring beans (see there).</p>
     */
    public RepositoryFactory guard(RepositoryFactory factory) {
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(factory);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAspect(this);
        return (RepositoryFactory) proxyFactory.getProxy();
    }

    @Around("execution(public * org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory.*(..))")
    public Object guardRepositories(ProceedingJoinPoint joinPoint) throws Throwable {
        // The repositories that RepositoryFactory makes are not Spring beans (it makes them with 'new', one for each request and
        // each user), so Spring does not wrap them and the aspect of the beans does not reach them. What the factory returns is
        // wrapped here instead, by the same advice (check). This part, and 'guard' below, are a way round that: if the
        // repositories become Spring beans, Spring wraps them like any other bean, and they can be changed or removed.
        Object made = joinPoint.proceed();
        if (made instanceof JooqBaseRepository repository && joinPoint.getThis() instanceof RepositoryFactory wrapped) {
            repository.useRepositoryFactory(wrapped);
            Class<?> type = ((MethodSignature) joinPoint.getSignature()).getReturnType();
            return guard(made, type);
        }
        return made;
    }

    /** Gives the repository the check of {@link AccessControl}: what is returned is called in its place, by its interface. */
    @SuppressWarnings("unchecked")
    public <T> T guard(T repository, Class<?> repositoryInterface) {
        if (!hasMarkedMethod(repository.getClass())) {
            return repository;
        }
        if (!repositoryInterface.isInterface()) {
            throw new IllegalStateException(repository.getClass().getSimpleName() + " has a method marked with @AccessControl, "
                    + "so it has to be asked for by an interface, and " + repositoryInterface.getSimpleName() + " is not one.");
        }
        AspectJProxyFactory factory = new AspectJProxyFactory(repository);
        factory.setInterfaces(repositoryInterface);
        factory.addAspect(this);
        return (T) factory.getProxy();
    }

    /** Whether a class has a method marked with {@link AccessControl}, itself or inherited. */
    public boolean hasMarkedMethod(Class<?> type) {
        return marked.computeIfAbsent(type, t -> Arrays.stream(t.getMethods())
                .anyMatch(method -> method.isAnnotationPresent(AccessControl.class)));
    }

    @Around("@annotation(accessControl)")
    public Object check(ProceedingJoinPoint joinPoint, AccessControl accessControl) throws Throwable {
        if (!accessControl.ignore()) {
            ScoreUser requester = requesterOf(joinPoint);
            if (requester == null) {
                throw new AccessControlException("Not allowed to access without a granted requester.");
            }
            ScoreRole[] requiredAnyRole = accessControl.requiredAnyRole();
            if (requiredAnyRole.length > 0 && !requester.hasAnyRole(requiredAnyRole)) {
                throw new AccessControlException(requester);
            }
        }
        return joinPoint.proceed();
    }

    private static ScoreUser requesterOf(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getTarget() instanceof JooqBaseRepository repository) {
            return repository.requester();
        }
        for (Object argument : joinPoint.getArgs()) {
            if (argument instanceof ScoreUser user) {
                return user;
            }
        }
        return null;
    }
}
