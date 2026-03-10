package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;

public class ACCGroup extends ACC {

    ACCGroup(AccSummaryRecord acc, ACC basedAcc,
             CcDocument ccDocument,
             SchemaNamingStrategy namingStrategy) {
        super(acc, basedAcc, ccDocument, namingStrategy);
    }
}
