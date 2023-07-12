package org.oagi.score.gateway.http.api.oas_management.data;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class BieForOasDocUpdateRequest {
    private List<BieForOasDoc> bieForOasDoclist = Collections.emptyList();

}
