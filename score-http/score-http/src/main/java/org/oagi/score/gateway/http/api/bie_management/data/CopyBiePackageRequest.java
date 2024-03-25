package org.oagi.score.gateway.http.api.bie_management.data;

import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CopyBiePackageRequest {

    private ScoreUser requester;

    private List<BigInteger> biePackageIdList = Collections.emptyList();

    public CopyBiePackageRequest() {
    }

    public CopyBiePackageRequest(ScoreUser requester) {
        this.requester = requester;
    }

    public ScoreUser getRequester() {
        return requester;
    }

    public void setRequester(ScoreUser requester) {
        this.requester = requester;
    }

    public List<BigInteger> getBiePackageIdList() {
        return biePackageIdList;
    }

    public void setBiePackageId(BigInteger biePackageId) {
        setBiePackageIdList(Arrays.asList(biePackageId));
    }

    public void setBiePackageIdList(List<BigInteger> biePackageIdList) {
        this.biePackageIdList = biePackageIdList;
    }

}
