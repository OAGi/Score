package org.oagi.score.data;

import lombok.Data;

import java.util.Date;

@Data
public class BBIEP implements BIE {
    private long bbiepId;
    private String guid;
    private long basedBccpId;
    private String definition;
    private String remark;
    private String bizTerm;
    private long createdBy;
    private long lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private long ownerTopLevelAsbiepId;
}
