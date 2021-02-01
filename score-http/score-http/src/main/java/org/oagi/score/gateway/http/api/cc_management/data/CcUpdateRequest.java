package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcAccNodeDetail;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcAsccpNodeDetail;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcBccpNodeDetail;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcBdtScNodeDetail;

import java.util.Collections;
import java.util.List;

@Data
public class CcUpdateRequest {
    private List<CcAccNodeDetail> accNodeDetails = Collections.emptyList();
    private List<CcAsccpNodeDetail> asccpNodeDetails = Collections.emptyList();
    private List<CcBccpNodeDetail> bccpNodeDetails = Collections.emptyList();
    private List<CcBdtScNodeDetail> bdtNodeDetails = Collections.emptyList();
}
