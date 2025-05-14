package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;

public class ASCCPComplexType extends ASCCP {

    ASCCPComplexType(AsccpSummaryRecord asccp, AccSummaryRecord roleOfAcc) {
        super(asccp, roleOfAcc);
    }

}
