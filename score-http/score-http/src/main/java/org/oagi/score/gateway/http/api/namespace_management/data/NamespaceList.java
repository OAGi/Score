package org.oagi.score.gateway.http.api.namespace_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class NamespaceList {

    private BigInteger namespaceId = BigInteger.ZERO;
    private BigInteger libraryId;
    private String uri;
    private String prefix;
    private String owner;
    private String description;
    private boolean isStd;
    private Date lastUpdateTimestamp;
    private String lastUpdateUser;
}
