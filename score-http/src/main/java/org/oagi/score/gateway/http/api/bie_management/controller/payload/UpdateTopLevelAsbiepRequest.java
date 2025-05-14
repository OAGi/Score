package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

public class UpdateTopLevelAsbiepRequest {

    private final TopLevelAsbiepId topLevelAsbiepId;
    private final String status;
    private final String version;
    private final Boolean inverseMode;

    public UpdateTopLevelAsbiepRequest(TopLevelAsbiepId topLevelAsbiepId, String status, String version,
                                       Boolean inverseMode) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.status = status;
        this.version = version;
        this.inverseMode = inverseMode;
    }

    public TopLevelAsbiepId getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public String getStatus() {
        return status;
    }

    public String getVersion() {
        return version;
    }

    public Boolean getInverseMode() {
        return inverseMode;
    }
}
