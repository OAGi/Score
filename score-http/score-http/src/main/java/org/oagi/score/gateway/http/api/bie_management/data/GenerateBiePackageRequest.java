package org.oagi.score.gateway.http.api.bie_management.data;

import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.List;

import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;

public class GenerateBiePackageRequest {

    private ScoreUser requester;

    private BigInteger biePackageId;

    private List<BigInteger> topLevelAsbiepIdList;

    private String schemaExpression = "XML";

    public GenerateBiePackageRequest() {
    }

    public GenerateBiePackageRequest(ScoreUser requester) {
        setRequester(requester);
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
        if (topLevelAsbiepIdList != null) {
            this.topLevelAsbiepIdList = topLevelAsbiepIdList;
        }
    }

    public String getSchemaExpression() {
        return schemaExpression;
    }

    public void setSchemaExpression(String schemaExpression) {
        if (hasLength(schemaExpression)) {
            this.schemaExpression = schemaExpression;
        }
    }
}
