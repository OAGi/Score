package org.oagi.score.gateway.http.api.cc_management.propagation;

import org.oagi.score.gateway.http.api.cc_management.model.CcRecordTypes;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.change.RecordType;
import org.springframework.stereotype.Component;

/** BCC.den is built from the from-ACC's object class term and the to-BCCP's den (which changes with its property term). */
@Component
public final class BccPropagationHandler extends LookupPropagationHandler {

    public BccPropagationHandler(CcDependencyLookup lookup) {
        super("BCC", CcRecordTypes.BCC,
                new Rule<>(CcRecordTypes.BCCP, BccpManifestId::new, lookup::bccsToBccp)
                        .whenChanged("property_term", "den").producing("den"),
                new Rule<>(CcRecordTypes.ACC, AccManifestId::new, lookup::bccsFromAcc)
                        .whenChanged("object_class_term").producing("den"));
    }
}
