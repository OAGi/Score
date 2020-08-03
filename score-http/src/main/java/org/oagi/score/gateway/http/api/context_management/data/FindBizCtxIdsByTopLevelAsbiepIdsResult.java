package org.oagi.score.gateway.http.api.context_management.data;

import lombok.Data;

@Data
public class FindBizCtxIdsByTopLevelAsbiepIdsResult {
    private long topLevelAsbiepId;
    private long bizCtxId;
    private String name;
}
