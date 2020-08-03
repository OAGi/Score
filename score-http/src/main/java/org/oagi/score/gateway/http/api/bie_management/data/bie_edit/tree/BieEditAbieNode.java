package org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditNode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAbieNode extends BieEditNode {

    private long asbiepId;
    private long abieId;
    private long asccpId;
    private long accId;
    private long ownerUserId;
    private String access;

}
