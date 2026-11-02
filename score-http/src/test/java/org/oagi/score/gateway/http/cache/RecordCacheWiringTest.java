package org.oagi.score.gateway.http.cache;

import org.oagi.score.gateway.http.api.cc_management.repository.AffectedRecordsRepository;
import org.oagi.score.gateway.http.change.ChangeLogReader;
import org.oagi.score.gateway.http.change.RecordChangeWatchdog;
import org.oagi.score.gateway.http.change.repository.RecordChangeLogRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListRecordKeys;
import org.oagi.score.gateway.http.api.cc_management.model.CcRecordKeys;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListRecordKeys;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceRecordKeys;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtRecordKeys;
import org.oagi.score.gateway.http.api.cc_management.propagation.CcDependencyLookup;
import org.oagi.score.gateway.http.change.propagation.ChangePropagator;
import org.oagi.score.gateway.http.change.propagation.PropagationConsumer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The beans of the record cache find each other: the provider chosen by {@code spring.cache.type} (Caffeine when none is given) is the record cache,
 * the watchdog is its observer, and the resolver of the records is among the watchdog's.
 */
class RecordCacheWiringTest {

    @Test
    void theBeansAreWiredOnTheDefaultProvider() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.recordChangeLogRepository()).thenReturn(mock(RecordChangeLogRepository.class));
        when(repositoryFactory.affectedRecordsRepository()).thenReturn(mock(AffectedRecordsRepository.class));

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(RepositoryFactory.class, () -> repositoryFactory);
            context.registerBean(CcDependencyLookup.class, () -> mock(CcDependencyLookup.class));
            context.scan("org.oagi.score.gateway.http.cache", "org.oagi.score.gateway.http.change",
                    "org.oagi.score.gateway.http.api.cc_management.propagation", "org.oagi.score.gateway.http.record");
            context.registerBean(CcRecordKeys.class);
            context.registerBean(NamespaceRecordKeys.class);
            context.registerBean(CodeListRecordKeys.class);
            context.registerBean(AgencyIdListRecordKeys.class);
            context.registerBean(XbtRecordKeys.class);
            context.refresh();

            assertInstanceOf(CaffeineRecordCache.class, context.getBean(RecordCache.class));
            assertEquals(1, context.getBeansOfType(RecordChangeKeyResolver.class).size());
            assertEquals(1, context.getBeansOfType(TableWriteListener.class).size());
            assertEquals(1, context.getBeansOfType(ChangeKeyResolver.class).size());
            assertEquals(1, context.getBeansOfType(ChangePropagator.class).size());
            assertInstanceOf(RecordChangeWatchdog.class, context.getBean(ChangeLogReader.class));
            assertEquals(1, context.getBeansOfType(RecordCacheInvalidator.class).size());
            assertInstanceOf(WriteCatchUp.class, context.getBean(RecordCacheWriteObserver.class));
            assertInstanceOf(CacheInvalidationConsumer.class, context.getBeansOfType(PropagationConsumer.class).values().iterator().next());
            assertEquals(4, context.getBeansOfType(org.oagi.score.gateway.http.change.propagation.PropagationHandler.class).size());
        }
    }
}
