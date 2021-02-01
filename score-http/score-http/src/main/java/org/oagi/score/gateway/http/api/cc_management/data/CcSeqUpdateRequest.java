package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;

@Data
public class CcSeqUpdateRequest {

    private CcId item;
    private CcId after;

}
