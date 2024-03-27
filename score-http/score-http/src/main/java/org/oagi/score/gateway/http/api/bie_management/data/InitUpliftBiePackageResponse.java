package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class InitUpliftBiePackageResponse {

    private BigInteger upliftedBiePackageId;

    private List<BigInteger> sourceTopLevelAsbiepIdList;

    public InitUpliftBiePackageResponse(BigInteger upliftedBiePackageId, List<BigInteger> sourceTopLevelAsbiepIdList) {
        this.upliftedBiePackageId = upliftedBiePackageId;
        this.sourceTopLevelAsbiepIdList = sourceTopLevelAsbiepIdList;
    }
}
