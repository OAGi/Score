package org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditNode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditBbieScNode extends BieEditNode {

    private long bbieScId;
    private long dtScId;

}
