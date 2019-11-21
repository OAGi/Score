package org.oagi.srt.gateway.http.api.release_management.data;

import lombok.Data;

import java.util.Date;

@Data
public class ReleaseList {

    private long releaseId;
    private String releaseNum;
    private int rawState;
    private String state;
    private String namespace;
    private String lastUpdatedBy;
    private Date lastUpdateTimestamp;

}
