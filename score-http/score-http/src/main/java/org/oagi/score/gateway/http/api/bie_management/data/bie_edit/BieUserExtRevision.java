package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BieUserExtRevision {

    private BigInteger bieUserExtRevisionId;
    private BigInteger extAbieId;
    private BigInteger extAccId;
    private BigInteger userExtAccId;
    private BigInteger topLevelAsbiepId;
}
