package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcAsccpNodeDetail;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcBccpNodeDetail;

import java.util.Collections;
import java.util.List;

@Data
public class ExtensionUpdateRequest {

    private long extensionId;
    private long releaseId;
    private List<CcAsccpNodeDetail> asccpDetails = Collections.emptyList();
    private List<CcBccpNodeDetail> bccpDetails = Collections.emptyList();

}
