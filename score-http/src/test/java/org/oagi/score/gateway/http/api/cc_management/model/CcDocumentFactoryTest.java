package org.oagi.score.gateway.http.api.cc_management.model;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCache;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CcDocumentFactoryTest {

    @Test
    void withACacheTheDocumentIsTheCachedOneWithTheFallback() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        CcCache cache = mock(CcCache.class);
        when(repositoryFactory.ccCache()).thenReturn(cache);

        CcDocument document = new CcDocumentFactory(repositoryFactory)
                .create(mock(ScoreUser.class), new ReleaseId(BigInteger.ONE));

        assertInstanceOf(DelegatingCcDocument.class, document);
    }

}
