package org.oagi.srt.gateway.http.api.module_management.data;

import lombok.Data;

import java.util.Date;

@Data
public class ModuleList {

    private long moduleId;
    private String module;
    private String namespace;
    private long ownerUserId;
    private String owner;
    private String lastUpdatedBy;
    private Date lastUpdateTimestamp;
    private String sinceRelease;
    private boolean canEdit;

}
