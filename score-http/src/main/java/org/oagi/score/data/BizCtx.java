package org.oagi.score.data;

import lombok.Data;

import java.util.Date;

@Data
public class BizCtx {
    private long bizCtxId;
    private String guid;
    private String name;
    private long createdBy;
    private long lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
}
