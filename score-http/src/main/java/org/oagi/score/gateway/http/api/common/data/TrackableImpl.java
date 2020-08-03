package org.oagi.score.gateway.http.api.common.data;

import lombok.Data;
import org.oagi.score.data.Trackable;

@Data
public abstract class TrackableImpl implements Trackable {

    private Long releaseId;
    private int revisionNum;
    private int revisionTrackingNum;

}
