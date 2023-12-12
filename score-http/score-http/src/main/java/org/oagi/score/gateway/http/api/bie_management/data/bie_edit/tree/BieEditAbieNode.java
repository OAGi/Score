package org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditNode;
import org.oagi.score.service.common.data.AccessPrivilege;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAbieNode extends BieEditNode {

    private BigInteger asbiepId;
    private BigInteger abieId;
    private BigInteger asccpManifestId;
    private BigInteger accManifestId;
    private BigInteger ownerUserId;
    private String loginId;
    private String releaseNum;
    private AccessPrivilege access;
    private boolean deprecated;
    private String deprecatedReason;
    private String deprecatedRemark;
    private boolean inverseMode;

}
