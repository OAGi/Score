package org.oagi.score.gateway.http.api.oas_management.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.repo.api.openapidoc.model.BieForOasDoc;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BieForOasDocUpdateRequest {
    private BigInteger oasDocId;
    private List<BieForOasDoc> bieForOasDocList = Collections.emptyList();
}
