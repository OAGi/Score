package org.oagi.srt.gateway.http.api.common.data;

import lombok.Data;
import org.oagi.srt.data.Trackable;

@Data
public abstract class TrackableImpl implements Trackable {

    private Long releaseId;
    private int revisionNum;
    private int revisionTrackingNum;

}
