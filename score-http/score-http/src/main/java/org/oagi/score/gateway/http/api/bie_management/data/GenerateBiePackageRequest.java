package org.oagi.score.gateway.http.api.bie_management.data;

import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.List;

public class GenerateBiePackageRequest {

    private ScoreUser requester;

    private BigInteger biePackageId;

    private List<BigInteger> topLevelAsbiepIdList;

    private String schemaExpression;

    public GenerateBiePackageRequest() {
    }

    public GenerateBiePackageRequest(ScoreUser requester) {
        this.requester = requester;
    }

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

    public String getSchemaExpression() {
        return schemaExpression;
    }

    public void setSchemaExpression(String schemaExpression) {
        this.schemaExpression = schemaExpression;
    }
}
