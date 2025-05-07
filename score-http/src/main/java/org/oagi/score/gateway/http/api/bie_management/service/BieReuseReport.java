package org.oagi.score.gateway.http.api.bie_management.service;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BieReuseReport {
    private BigInteger reusingTopLevelAsbiepId;
    private String reusingState;
    private Boolean reusingDeprecated;
    private String reusingDeprecatedReason;
    private String reusingDeprecatedRemark;
    private String reusingPropertyTerm;
    private String reusingDisplayName;
    private String reusingDen;
    private String reusingGuid;
    private String reusingOwner;
    private String reusingVersion;
    private String reusingStatus;
    private String reusingRemark;
    private String path;
    private String displayPath;

    private BigInteger reusedTopLevelAsbiepId;
    private String reusedState;
    private Boolean reusedDeprecated;
    private String reusedDeprecatedReason;
    private String reusedDeprecatedRemark;
    private String reusedPropertyTerm;
    private String reusedDisplayName;
    private String reusedDen;
    private String reusedGuid;
    private String reusedOwner;
    private String reusedVersion;
    private String reusedStatus;
    private String reusedRemark;

    private String releaseNum;
}
