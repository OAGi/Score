package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccId;

import java.math.BigInteger;

@Data
public class BieUserExtRevision {

    private BigInteger bieUserExtRevisionId;
    private AbieId extAbieId;
    private AccId extAccId;
    private AccId userExtAccId;
    private TopLevelAsbiepId topLevelAsbiepId;
}
