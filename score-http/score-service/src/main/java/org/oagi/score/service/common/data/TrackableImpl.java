package org.oagi.score.service.common.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public abstract class TrackableImpl implements Trackable {

    private BigInteger libraryId;
    private BigInteger releaseId;
    private String releaseNum;
    private boolean workingRelease;

    private BigInteger logId;
    private int revisionNum;
    private int revisionTrackingNum;

}
