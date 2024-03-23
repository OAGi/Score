package org.oagi.score.gateway.http.api.bie_management.data;

import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeleteBiePackageRequest {

    private ScoreUser requester;

    private List<BigInteger> biePackageIdList = new ArrayList<>();

    public DeleteBiePackageRequest() {
    }

    public DeleteBiePackageRequest(ScoreUser requester) {
        setRequester(requester);
    }

    public ScoreUser getRequester() {
        return requester;
    }

    public void setRequester(ScoreUser requester) {
        this.requester = requester;
    }

    public void setBiePackageId(BigInteger biePackageId) {
        setBiePackageIdList(Arrays.asList(biePackageId));
    }

    public List<BigInteger> getBiePackageIdList() {
        return biePackageIdList;
    }

    public void setBiePackageIdList(List<BigInteger> biePackageIdList) {
        this.biePackageIdList = biePackageIdList;
    }

}
