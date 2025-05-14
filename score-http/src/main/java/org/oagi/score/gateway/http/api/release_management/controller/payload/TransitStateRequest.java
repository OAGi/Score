package org.oagi.score.gateway.http.api.release_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

@Data
public class TransitStateRequest {

    private ReleaseId releaseId;
    private String state;

    private ReleaseValidationRequest validationRequest;
}
