package org.oagi.srt.gateway.http.api.bie_management.data;

import lombok.Data;

import java.util.List;

@Data
public class BieCopyRequest {

    private long topLevelAbieId;
    private List<Long> bizCtxIds;

}
