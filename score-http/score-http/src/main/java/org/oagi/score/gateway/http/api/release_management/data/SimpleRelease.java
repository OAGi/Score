package org.oagi.score.gateway.http.api.release_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class SimpleRelease {

    private BigInteger releaseId;
    private String releaseNum;
    private ReleaseState state;
    private boolean workingRelease;

}
