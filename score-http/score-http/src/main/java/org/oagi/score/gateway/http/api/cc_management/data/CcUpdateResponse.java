package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcAccNodeDetail;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcAsccpNodeDetail;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcBccpNodeDetail;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcBdtScNodeDetail;

import java.util.List;

@Data
public class CcUpdateResponse {

    private List<CcAccNodeDetail> accNodeResults;
    private List<CcAsccpNodeDetail> asccpNodeResults;
    private List<CcBccpNodeDetail> bccpNodeResults;
    private List<CcBdtScNodeDetail> bdtScNodeResults;
}
