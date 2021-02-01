package org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditNode;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditBbiepNode extends BieEditNode {

    private BigInteger bbieId = BigInteger.ZERO;
    private BigInteger bccManifestId = BigInteger.ZERO;
    private BigInteger bbiepId = BigInteger.ZERO;
    private BigInteger bccpManifestId = BigInteger.ZERO;
    private BigInteger bdtManifestId = BigInteger.ZERO;
    private boolean attribute;

}
