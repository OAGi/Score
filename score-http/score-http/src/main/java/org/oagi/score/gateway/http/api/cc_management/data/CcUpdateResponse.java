package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.node.*;

import java.util.List;

@Data
public class CcUpdateResponse {

    private List<CcAccNodeDetail> accNodeResults;
    private List<CcAsccpNodeDetail> asccpNodeResults;
    private List<CcBccpNodeDetail> bccpNodeResults;
    private List<CcBdtNodeDetail> dtNodeResults;
    private List<CcBdtScNodeDetail> bdtScNodeResults;
}
