package org.oagi.score.repository;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface ScoreRepository<T> {

    List<T> findAll();

    default List<T> findAllByReleaseId(BigInteger releaseId) {
        if (releaseId == null) {
            return Collections.emptyList();
        }
        return findAllByReleaseIds(Arrays.asList(releaseId));
    }

    default List<T> findAllByReleaseIds(Collection<BigInteger> releaseIds) {
        return Collections.emptyList();
    }

    T findById(BigInteger id);

}
