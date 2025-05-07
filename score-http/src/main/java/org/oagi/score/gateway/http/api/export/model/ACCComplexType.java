package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;

public class ACCComplexType extends ACC {

    ACCComplexType(AccSummaryRecord acc, ACC basedAcc,
                   CcDocument ccDocument) {
        super(acc, basedAcc, ccDocument);
    }

}
