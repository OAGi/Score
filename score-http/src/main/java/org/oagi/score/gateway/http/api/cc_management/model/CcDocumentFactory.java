package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.api.cc_management.cache.CcCache;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.Arrays;
import java.util.Collection;

/**
 * Where the code that needs a {@link CcDocument} gets one: the document of the releases it asks for, answering from
 * the shared cache and delegating to the loaded releases ({@link CcDocumentImpl}) where the cache cannot answer
 * ({@link DelegatingCcDocument}).
 * <p>
 * Without a cache (a {@link RepositoryFactory} made outside Spring) there is nothing to answer from, so the releases
 * are loaded at once, as they always were.
 */
public class CcDocumentFactory {

    private final RepositoryFactory repositoryFactory;

    public CcDocumentFactory(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public CcDocument create(ScoreUser requester, ReleaseId releaseId) {
        return create(requester, Arrays.asList(releaseId));
    }

    public CcDocument create(ScoreUser requester, Collection<ReleaseId> releaseIds) {
        if (repositoryFactory.ccCache() == CcCache.DISABLED) {
            return new CcDocumentImpl(requester, repositoryFactory, releaseIds);
        }
        return new DelegatingCcDocument(new CachedCcDocument(requester, repositoryFactory, releaseIds),
                () -> new CcDocumentImpl(requester, repositoryFactory, releaseIds));
    }

}
