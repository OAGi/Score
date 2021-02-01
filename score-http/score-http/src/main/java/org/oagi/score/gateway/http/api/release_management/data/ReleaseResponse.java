package org.oagi.score.gateway.http.api.release_management.data;

import lombok.Data;

@Data
public class ReleaseResponse {
    private ReleaseDetail releaseDetail;
    private String status;
    private String statusMessage;
}
