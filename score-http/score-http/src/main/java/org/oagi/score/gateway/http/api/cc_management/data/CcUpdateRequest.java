package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.node.*;

import java.util.Collections;
import java.util.List;

@Data
public class CcUpdateRequest {
    private List<CcAccNodeDetail> accNodeDetails = Collections.emptyList();
    private List<CcAsccpNodeDetail> asccpNodeDetails = Collections.emptyList();
    private List<CcBccpNodeDetail> bccpNodeDetails = Collections.emptyList();
    private List<CcBdtNodeDetail> dtNodeDetails = Collections.emptyList();
    private List<CcBdtScNodeDetail> dtScNodeDetails = Collections.emptyList();
}
