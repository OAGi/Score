package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.cache.RecordCacheProperties;

import org.oagi.score.gateway.http.cache.RecordCache;

import org.junit.jupiter.api.Test;
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
        RecordCache cache = mock(RecordCache.class);
        when(repositoryFactory.recordCache()).thenReturn(cache);
        when(repositoryFactory.recordCacheProperties()).thenReturn(new RecordCacheProperties());

        CcDocument document = new CcDocumentFactory(repositoryFactory)
                .create(mock(ScoreUser.class), new ReleaseId(BigInteger.ONE));

        assertInstanceOf(CacheFirstCcDocument.class, document);
    }

}
