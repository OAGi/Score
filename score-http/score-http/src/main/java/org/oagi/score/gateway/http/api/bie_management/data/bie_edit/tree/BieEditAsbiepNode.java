package org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditNode;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAsbiepNode extends BieEditNode {

    private BigInteger asbieId = BigInteger.ZERO;
    private BigInteger asccManifestId = BigInteger.ZERO;
    private BigInteger asbiepId = BigInteger.ZERO;
    private BigInteger asccpManifestId = BigInteger.ZERO;
    private BigInteger abieId = BigInteger.ZERO;
    private BigInteger accManifestId = BigInteger.ZERO;

}
