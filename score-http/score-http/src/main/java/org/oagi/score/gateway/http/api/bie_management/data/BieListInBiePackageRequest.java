package org.oagi.score.gateway.http.api.bie_management.data;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class BieListInBiePackageRequest extends PaginationRequest<BieList> {

    private BigInteger biePackageId;

    public BieListInBiePackageRequest(ScoreUser requester) {
        super(requester, BieList.class);
    }

    public BigInteger getBiePackageId() {
        return biePackageId;
    }

    public void setBiePackageId(BigInteger biePackageId) {
        this.biePackageId = biePackageId;
    }
}
