package org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditNode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAsbiepNode extends BieEditNode {

    private long asbieId;
    private long asccId;
    private long asbiepId;
    private long asccpId;
    private long abieId;
    private long accId;

}
