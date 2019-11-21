package org.oagi.srt.gateway.http.api.cc_management.data;

import lombok.Data;

@Data
public class CcActionRequest {
    private String action;
    private String type;
    private Long id;
}
