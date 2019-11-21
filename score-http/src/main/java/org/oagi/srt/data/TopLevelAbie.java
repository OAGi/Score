package org.oagi.srt.data;

import lombok.Data;

import java.util.Date;

@Data
public class TopLevelAbie {

    private long topLevelAbieId;
    private Long abieId;
    private long ownerUserId;
    private long releaseId;
    private int state;
    private long lastUpdatedBy;
    private Date lastUpdateTimestamp;

}
