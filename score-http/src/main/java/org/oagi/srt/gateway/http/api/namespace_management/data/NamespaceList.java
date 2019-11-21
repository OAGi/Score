package org.oagi.srt.gateway.http.api.namespace_management.data;

import lombok.Data;

import java.util.Date;

@Data
public class NamespaceList {

    private long namespaceId;
    private String uri;
    private String prefix;
    private long ownerUserId;
    private String owner;
    private String description;
    private Date lastUpdateTimestamp;
    private boolean canEdit;
}
