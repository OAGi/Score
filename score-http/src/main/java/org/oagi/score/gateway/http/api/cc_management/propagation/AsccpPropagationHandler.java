package org.oagi.score.gateway.http.api.cc_management.propagation;

import org.oagi.score.gateway.http.api.cc_management.model.CcRecordTypes;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.change.RecordType;
import org.springframework.stereotype.Component;

/** ASCCP.den is built from the asccp's property term and the role-of ACC's object class term; this handler is for the second. */
@Component
public final class AsccpPropagationHandler extends LookupPropagationHandler {

    public AsccpPropagationHandler(CcDependencyLookup lookup) {
        super("ASCCP", CcRecordTypes.ASCCP,
                new Rule<>(CcRecordTypes.ACC, AccManifestId::new, lookup::asccpsRoleOf)
                        .whenChanged("object_class_term").producing("den"));
    }
}
