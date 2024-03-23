package org.oagi.score.gateway.http.api.bie_management.data;

import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class AddBieToBiePackageRequest {

    private ScoreUser requester;

    private BigInteger biePackageId;

    private List<BigInteger> topLevelAsbiepIdList = new ArrayList<>();

    public ScoreUser getRequester() {
        return requester;
    }

    public void setRequester(ScoreUser requester) {
        this.requester = requester;
    }

    public BigInteger getBiePackageId() {
        return biePackageId;
    }

    public void setBiePackageId(BigInteger biePackageId) {
        this.biePackageId = biePackageId;
    }

    public List<BigInteger> getTopLevelAsbiepIdList() {
        return topLevelAsbiepIdList;
    }

    public void setTopLevelAsbiepIdList(List<BigInteger> topLevelAsbiepIdList) {
        this.topLevelAsbiepIdList = topLevelAsbiepIdList;
    }
}
