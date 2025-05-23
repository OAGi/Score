package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
public class BieForOasDocUpdateRequest {

    private BigInteger oasDocId;

    private List<BieForOasDoc> bieForOasDocList;

    public BieForOasDocUpdateRequest() {
        this.bieForOasDocList = Collections.emptyList();
    }

    public BieForOasDocUpdateRequest(List<BieForOasDoc> bieForOasDocList) {
        this.bieForOasDocList = bieForOasDocList;
    }

}
