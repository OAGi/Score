package org.oagi.score.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode
public class TopLevelAsbiep {

    private long topLevelAsbiepId;
    private Long asbiepId;
    private long ownerUserId;
    private long releaseId;
    private int state;
    private long lastUpdatedBy;
    private Date lastUpdateTimestamp;

}
