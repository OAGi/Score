package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.srt.gateway.http.api.bie_management.data.bie_edit.BieEditNode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditBbiepNode extends BieEditNode {

    private long bbieId;
    private long bccId;
    private long bbiepId;
    private long bccpId;
    private long bdtId;
    private boolean attribute;

}
