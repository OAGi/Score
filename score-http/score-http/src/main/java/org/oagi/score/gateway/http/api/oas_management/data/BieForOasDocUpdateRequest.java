package org.oagi.score.gateway.http.api.oas_management.data;

import lombok.Data;
import org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator;
import org.oagi.score.repo.api.openapidoc.model.BieForOasDoc;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
public class BieForOasDocUpdateRequest {
    private BigInteger oasDocId;
    private List<BieForOasDoc> bieForOasDocList = Collections.emptyList();

}
