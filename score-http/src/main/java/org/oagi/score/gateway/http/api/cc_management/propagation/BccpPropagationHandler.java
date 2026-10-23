package org.oagi.score.gateway.http.api.cc_management.propagation;

import org.oagi.score.gateway.http.api.cc_management.model.CcRecordTypes;

import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.change.RecordType;
import org.springframework.stereotype.Component;

/** BCCP.den is built from the bccp's property term and the qualifier and data type term of its data type; this handler is for the second. */
@Component
public final class BccpPropagationHandler extends LookupPropagationHandler {

    public BccpPropagationHandler(CcDependencyLookup lookup) {
        super("BCCP", CcRecordTypes.BCCP,
                new Rule<>(CcRecordTypes.DT, DtManifestId::new, lookup::bccpsOfDt)
                        .whenChanged("qualifier", "data_type_term", "den").producing("den", "representation_term"));
    }
}
