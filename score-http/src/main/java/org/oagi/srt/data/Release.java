package org.oagi.srt.data;

import lombok.Data;

import java.util.Date;

@Data
public class Release {

    private long releaseId;
    private String releaseNum;
    private String releaseNote;
    private long namespaceId;
    private long createdBy;
    private long lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private Integer state;

}
