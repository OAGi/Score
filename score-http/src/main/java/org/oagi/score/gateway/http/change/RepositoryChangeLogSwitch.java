package org.oagi.score.gateway.http.change;

import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** The change log switch of the database: asked for lazily, as the repositories use the cache that uses this. */
@Component
public class RepositoryChangeLogSwitch implements ChangeLogSwitch {

    private final ObjectProvider<RepositoryFactory> repositoryFactory;

    public RepositoryChangeLogSwitch(ObjectProvider<RepositoryFactory> repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    @Override
    public void stopWriting() {
        repositoryFactory.getObject().recordChangeLogRepository().stopWriting();
    }

    @Override
    public void resumeWriting() {
        repositoryFactory.getObject().recordChangeLogRepository().resumeWriting();
    }
}
