package org.oagi.srt.gateway.http.api.context_management.data;

import lombok.Data;

@Data
public class FindBizCtxIdsByTopLevelAbieIdsResult {
    private long topLevelAbieId;
    private long bizCtxId;
    private String name;
}
