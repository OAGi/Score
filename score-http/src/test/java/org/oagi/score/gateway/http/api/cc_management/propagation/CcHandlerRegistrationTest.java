package org.oagi.score.gateway.http.api.cc_management.propagation;

import org.oagi.score.gateway.http.api.cc_management.model.CcRecordTypes;

import org.junit.jupiter.api.Test;
import static org.oagi.score.gateway.http.change.TestIds.event;
import static org.oagi.score.gateway.http.change.TestIds.id;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.change.ChangeEvent;
import org.oagi.score.gateway.http.change.RecordType;
import org.oagi.score.gateway.http.change.propagation.PropagationEngine;
import org.oagi.score.gateway.http.change.propagation.PropagationHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The handlers are beans of their own, and the engine takes every handler that is a bean. */
class CcHandlerRegistrationTest {

    @Test
    void everyHandlerIsRegisteredAndTheEngineRunsThem() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(CcDependencyLookup.class, () -> new EmptyLookup());
            context.scan("org.oagi.score.gateway.http.api.cc_management.propagation", "org.oagi.score.gateway.http.change.propagation");
            context.refresh();

            Set<String> names = context.getBeansOfType(PropagationHandler.class).values().stream()
                    .map(PropagationHandler::name).collect(Collectors.toSet());
            assertEquals(Set.of("ASCCP", "ASCC", "BCCP", "BCC"), names);

            // a change nobody depends on reaches the handlers and ends
            PropagationEngine engine = context.getBean(PropagationEngine.class);
            assertEquals(List.of(), engine.propagate(List.of(event(CcRecordTypes.ACC, 1))).affected());
        }
    }

    private static final class EmptyLookup implements CcDependencyLookup {
        public List<Dependent<AccManifestId, AsccpManifestId>> asccpsRoleOf(Collection<AccManifestId> ids) { return List.of(); }
        public List<Dependent<AsccpManifestId, AsccManifestId>> asccsToAsccp(Collection<AsccpManifestId> ids) { return List.of(); }
        public List<Dependent<AccManifestId, AsccManifestId>> asccsFromAcc(Collection<AccManifestId> ids) { return List.of(); }
        public List<Dependent<BccpManifestId, BccManifestId>> bccsToBccp(Collection<BccpManifestId> ids) { return List.of(); }
        public List<Dependent<AccManifestId, BccManifestId>> bccsFromAcc(Collection<AccManifestId> ids) { return List.of(); }
        public List<Dependent<DtManifestId, BccpManifestId>> bccpsOfDt(Collection<DtManifestId> ids) { return List.of(); }
    }
}
