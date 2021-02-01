package org.oagi.score.gateway.http.api.release_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class ReleaseList {

    private BigInteger releaseId;
    private String guid;
    private String releaseNum;
    private String releaseNote;
    private ReleaseState state;

    private String createdBy;
    private Date creationTimestamp;

    private String lastUpdatedBy;
    private Date lastUpdateTimestamp;

}
