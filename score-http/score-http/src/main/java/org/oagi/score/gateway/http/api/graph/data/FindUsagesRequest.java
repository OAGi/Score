package org.oagi.score.gateway.http.api.graph.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class FindUsagesRequest {

    private String type;
    private BigInteger manifestId;

}
