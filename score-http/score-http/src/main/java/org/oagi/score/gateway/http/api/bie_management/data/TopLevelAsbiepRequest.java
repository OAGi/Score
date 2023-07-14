package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;
import org.oagi.score.repo.api.openapidoc.model.BieForOasDoc;

import java.math.BigInteger;

@Data
public class TopLevelAsbiepRequest {

    private BigInteger topLevelAsbiepId;
    private String version;
    private String status;
    private Boolean inverseMode;
    private BieForOasDoc bieForOasDoc;

}
