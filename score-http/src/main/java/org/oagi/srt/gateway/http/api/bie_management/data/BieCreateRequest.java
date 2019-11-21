package org.oagi.srt.gateway.http.api.bie_management.data;

import lombok.Data;

import java.util.List;

@Data
public class BieCreateRequest {

    private long asccpId;
    private long releaseId;
    private List<Long> bizCtxIds;

}
