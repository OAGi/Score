package org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.BieEditNode;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAbieNode extends BieEditNode {

    private AsbiepId asbiepId;
    private AbieId abieId;
    private AsccpManifestId asccpManifestId;
    private AccManifestId accManifestId;
    private UserId ownerUserId;
    private String loginId;
    private String releaseNum;
    private AccessPrivilege access;
    private boolean deprecated;
    private String deprecatedReason;
    private String deprecatedRemark;
    private boolean inverseMode;

}
