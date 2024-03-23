package org.oagi.score.gateway.http.api.bie_management.data;

import java.math.BigInteger;

public class CreateBiePackageResponse {

    private BigInteger biePackageId;

    public CreateBiePackageResponse(BigInteger biePackageId) {
        this.biePackageId = biePackageId;
    }

    public BigInteger getBiePackageId() {
        return biePackageId;
    }

}
