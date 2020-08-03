package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.BieEditAbieNodeDetail;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.BieEditAsbiepNodeDetail;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.BieEditBbieScNodeDetail;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.BieEditBbiepNodeDetail;

import java.util.Collections;
import java.util.List;

@Data
public class BieEditUpdateRequest {

    private long topLevelAsbiepId;
    private BieEditAbieNodeDetail abieNodeDetail;
    private List<BieEditAsbiepNodeDetail> asbiepNodeDetails = Collections.emptyList();
    private List<BieEditBbiepNodeDetail> bbiepNodeDetails = Collections.emptyList();
    private List<BieEditBbieScNodeDetail> bbieScNodeDetails = Collections.emptyList();

}
