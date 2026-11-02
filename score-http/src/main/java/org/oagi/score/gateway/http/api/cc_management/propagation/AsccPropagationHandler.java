package org.oagi.score.gateway.http.api.cc_management.propagation;

import org.oagi.score.gateway.http.api.cc_management.model.CcRecordTypes;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.change.RecordType;
import org.springframework.stereotype.Component;

/** ASCC.den is built from the from-ACC's object class term and the to-ASCCP's den (which changes with its property term). */
@Component
public final class AsccPropagationHandler extends LookupPropagationHandler {

    public AsccPropagationHandler(CcDependencyLookup lookup) {
        super("ASCC", CcRecordTypes.ASCC,
                new Rule<>(CcRecordTypes.ASCCP, AsccpManifestId::new, lookup::asccsToAsccp)
                        .whenChanged("property_term", "den").producing("den"),
                new Rule<>(CcRecordTypes.ACC, AccManifestId::new, lookup::asccsFromAcc)
                        .whenChanged("object_class_term").producing("den"));
    }
}
