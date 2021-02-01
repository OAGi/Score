package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcAsccpNodeDetail;


@Data
public class CcAsccpUpdateRequest {
    private long manifestId;
    private CcAsccpNodeDetail asccpNodeDetail;
}
