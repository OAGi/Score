package org.oagi.score.gateway.http.api.cc_management.propagation;

import org.oagi.score.gateway.http.change.propagation.PropagationHandler;

import java.util.List;

/** The handlers of the core component records, made by hand for a test that does not start Spring. */
final class CcHandlers {

    private CcHandlers() {
    }

    static List<PropagationHandler> of(CcDependencyLookup lookup) {
        return List.of(new AsccpPropagationHandler(lookup), new AsccPropagationHandler(lookup),
                new BccpPropagationHandler(lookup), new BccPropagationHandler(lookup));
    }
}
