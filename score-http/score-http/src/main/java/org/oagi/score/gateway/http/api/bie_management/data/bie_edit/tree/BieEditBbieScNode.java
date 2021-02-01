package org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditNode;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditBbieScNode extends BieEditNode {

    private BigInteger bbieScManifestId;
    private BigInteger dtScManifestId;

}
