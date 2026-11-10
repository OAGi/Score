package org.oagi.score.gateway.http.api.release_management.service;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseProgressRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Keeps a library from being changed while one of its releases is becoming a draft or being published.
 *
 * <p>What changes the core components, the code lists and the agency ID lists goes through three services. Before any of
 * their methods, the guard finds the library that is changed from what the call is given (a release, or the manifest of a
 * component), locks the library and its releases shared until the end of the transaction of the call, and refuses the call
 * when a release of the library is in the middle of a transition, or is taken hold of at this moment: the lock is not waited
 * for, since a lock that is not given is held by the release. The release that is asked to become a draft locks the same
 * rows exclusively before it reads anything, so it waits for the calls that are changing the library, and what it validates
 * is what has been committed; the calls that come after it find the release in the middle of its transition. A call that
 * gives nothing the guard can find a library of is refused when any library is in the middle of a transition.</p>
 *
 * <p>The work of the release itself changes the components, and is let through: it says so in the transaction it runs in
 * ({@link #asReleaseWork}), and the sign is gone when the transaction ends, wherever it runs.</p>
 */
@Aspect
@Component
@Order(ReleaseInProgressGuard.ORDER)
public class ReleaseInProgressGuard {

    /**
     * The transaction of a service has to begin before the guard is asked, so that the lock the guard takes is held until
     * the transaction ends: the order of the transaction advice is before the order of the guard.
     */
    public static final int TRANSACTION_ORDER = Ordered.LOWEST_PRECEDENCE - 20;

    static final int ORDER = Ordered.LOWEST_PRECEDENCE - 10;

    static final String MESSAGE = "A release of the library is being drafted or published. It cannot be changed until "
            + "that is done; try again in a moment.";

    private static final Object RELEASE_WORK = new Object();

    @Autowired
    private RepositoryFactory repositoryFactory;

    private ReleaseProgressRepository progress() {
        return repositoryFactory.releaseProgressRepository();
    }

    /** Runs what the release does to the components, in the transaction that is active. */
    public static void asReleaseWork(Runnable work) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("The work of a release is done in a transaction.");
        }
        TransactionSynchronizationManager.bindResource(RELEASE_WORK, Boolean.TRUE);
        try {
            work.run();
        } finally {
            TransactionSynchronizationManager.unbindResource(RELEASE_WORK);
        }
    }

    @Before("within(org.oagi.score.gateway.http.api.cc_management.service.CcCommandService) "
            + "|| within(org.oagi.score.gateway.http.api.code_list_management.service.CodeListCommandService) "
            + "|| within(org.oagi.score.gateway.http.api.agency_id_management.service.AgencyIdListCommandService)")
    public void beforeChange(JoinPoint joinPoint) {
        if (TransactionSynchronizationManager.hasResource(RELEASE_WORK)) {
            return;
        }
        Set<LibraryId> libraries = new LinkedHashSet<>();
        for (Object arg : joinPoint.getArgs()) {
            collect(arg, libraries, 0);
        }
        boolean busy;
        if (libraries.isEmpty()) {
            busy = progress().anyLibraryIsBusy();
        } else {
            busy = false;
            for (LibraryId library : libraries) {
                busy |= progress().libraryIsBusy(library);
            }
        }
        if (busy) {
            throw new IllegalArgumentException(MESSAGE);
        }
    }

    /** The libraries of what the call is given: a release, a library, the manifest of a component, in whatever it is wrapped in. */
    private void collect(Object arg, Set<LibraryId> libraries, int depth) {
        if (arg == null || depth > 3 || arg instanceof ScoreUser || arg instanceof CharSequence || arg instanceof Number
                || arg instanceof Boolean || arg instanceof Enum<?>) {
            return;
        }
        if (arg instanceof ReleaseId release) {
            progress().libraryOfRelease(release).ifPresent(libraries::add);
        } else if (arg instanceof LibraryId library) {
            libraries.add(library);
        } else if (arg instanceof ManifestId manifestId) {
            progress().libraryOfManifest(manifestId).ifPresent(libraries::add);
        } else if (arg instanceof Collection<?> collection) {
            collection.forEach(e -> collect(e, libraries, depth + 1));
        } else if (arg instanceof Map<?, ?> map) {
            map.values().forEach(e -> collect(e, libraries, depth + 1));
        } else if (arg.getClass().getName().startsWith("org.oagi.")) {
            for (Class<?> type = arg.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    try {
                        field.setAccessible(true);
                        collect(field.get(arg), libraries, depth + 1);
                    } catch (ReflectiveOperationException | RuntimeException ignored) {
                        // what cannot be read says nothing about the library
                    }
                }
            }
        }
    }
}
