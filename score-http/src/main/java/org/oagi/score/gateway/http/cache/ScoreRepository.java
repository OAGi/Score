package org.oagi.score.gateway.http.cache;

import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Deprecated
public interface ScoreRepository<T> {

    List<T> findAll();

    default List<T> findAllByReleaseId(ReleaseId releaseId) {
        if (releaseId == null) {
            return Collections.emptyList();
        }
        return findAllByReleaseIds(Arrays.asList(releaseId));
    }

    default List<T> findAllByReleaseIds(Collection<ReleaseId> releaseIds) {
        return Collections.emptyList();
    }

    T findById(BigInteger id);

}
