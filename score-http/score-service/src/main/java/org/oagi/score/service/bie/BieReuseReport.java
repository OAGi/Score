package org.oagi.score.service.bie;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BieReuseReport {
    private BigInteger reusingTopLevelAsbiepId;
    private String reusingState;
    private String reusingPropertyTerm;
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
    private String reusedPropertyTerm;
    private String reusedDen;
    private String reusedGuid;
    private String reusedOwner;
    private String reusedVersion;
    private String reusedStatus;
    private String reusedRemark;

    private String releaseNum;
}
