package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.util.List;

import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

public class GenerateBiePackageRequest {

    private ScoreUser requester;

    private BiePackageId biePackageId;

    private List<TopLevelAsbiepId> topLevelAsbiepIdList;

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

    public BiePackageId getBiePackageId() {
        return biePackageId;
    }

    public void setBiePackageId(BiePackageId biePackageId) {
        this.biePackageId = biePackageId;
    }

    public List<TopLevelAsbiepId> getTopLevelAsbiepIdList() {
        return topLevelAsbiepIdList;
    }

    public void setTopLevelAsbiepIdList(List<TopLevelAsbiepId> topLevelAsbiepIdList) {
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
