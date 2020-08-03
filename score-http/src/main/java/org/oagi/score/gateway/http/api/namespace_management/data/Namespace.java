package org.oagi.score.gateway.http.api.namespace_management.data;

import lombok.Data;

@Data
public class Namespace {

    private long namespaceId;
    private String uri;
    private String prefix;
    private String description;
    private long ownerUserId;
}
