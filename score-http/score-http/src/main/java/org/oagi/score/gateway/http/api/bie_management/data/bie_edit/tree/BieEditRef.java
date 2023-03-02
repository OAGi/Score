package org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;

@Data
@EqualsAndHashCode
public class BieEditRef {

    private BigInteger asbieId;
    private BigInteger basedAsccManifestId;
    private String hashPath;
    private BigInteger topLevelAsbiepId;
    private BigInteger refTopLevelAsbiepId;
    private boolean refInverseMode;

}
