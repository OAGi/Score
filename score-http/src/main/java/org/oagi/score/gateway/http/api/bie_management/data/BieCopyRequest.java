package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;

import java.util.List;

@Data
public class BieCopyRequest {

    private long topLevelAsbiepId;
    private List<Long> bizCtxIds;

}
