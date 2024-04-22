package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;

@Data
public class CcVerifyAppendResponse {
    private boolean warn;
    private String message;
}
