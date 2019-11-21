package org.oagi.srt.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.srt.gateway.http.api.cc_management.data.node.CcAccNodeDetail;
import org.oagi.srt.gateway.http.api.cc_management.data.node.CcAsccpNodeDetail;
import org.oagi.srt.gateway.http.api.cc_management.data.node.CcBccpNodeDetail;
import org.oagi.srt.gateway.http.api.cc_management.data.node.CcBdtScNodeDetail;

import java.util.Collections;
import java.util.List;

@Data
public class CcEditUpdateRequest {

    private long accId;
    private List<CcAccNodeDetail> accNodeDetails;
    private List<CcAsccpNodeDetail> asccpNodeResult = Collections.emptyList();
    private List<CcBccpNodeDetail> bccpNodeResults = Collections.emptyList();
    private List<CcBdtScNodeDetail> bdtScNodeResults = Collections.emptyList();

}
