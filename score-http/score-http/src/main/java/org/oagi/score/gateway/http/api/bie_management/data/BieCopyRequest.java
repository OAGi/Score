package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class BieCopyRequest {

    private BigInteger topLevelAsbiepId;
    private List<BigInteger> bizCtxIds;

}
