package org.oagi.score.gateway.http.api.oas_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.HashMap;

@Data
public class BieForOasDocUpdateRequest {
    private HashMap<BigInteger, String> resourceMap = new HashMap<>();
    private HashMap<BigInteger, String> operationIdMap = new HashMap<>();

}
