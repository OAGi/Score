package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
public class DeleteBieListRequest {

    private List<BigInteger> topLevelAsbiepIds = Collections.emptyList();

}
