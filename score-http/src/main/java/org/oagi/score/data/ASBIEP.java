package org.oagi.score.data;

import lombok.Data;

import java.util.Date;

@Data
public class ASBIEP implements BIE {
    private long asbiepId;
    private String guid;
    private long basedAsccpId;
    private long roleOfAbieId;
    private String definition;
    private String remark;
    private String bizTerm;
    private long createdBy;
    private long lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private long ownerTopLevelAsbiepId;
}
