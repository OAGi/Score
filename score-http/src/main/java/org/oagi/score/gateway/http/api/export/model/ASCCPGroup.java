package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;

public class ASCCPGroup extends ASCCP {

    ASCCPGroup(AsccpSummaryRecord asccp, AccSummaryRecord roleOfAcc) {
        super(asccp, roleOfAcc);
    }

}
