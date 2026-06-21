package org.oagi.score.gateway.http.api.oas_management.model;

import java.math.BigInteger;
import java.util.List;

/**
 * Issue #1729: one entry of an OpenAPI OAuth Flows Object for an {@code oauth2} security scheme
 * (implicit | password | clientCredentials | authorizationCode | deviceAuthorization). Each flow
 * carries its own URLs and a list of scopes.
 */
public class OasOAuthFlow {
    private BigInteger oasOAuthFlowId;
    private String guid;
    private String flowType;
    private String authorizationUrl;
    private String tokenUrl;
    private String refreshUrl;
    private String deviceAuthorizationUrl;
    private List<OasOAuthScope> scopes;

    public OasOAuthFlow() {
    }

    public BigInteger getOasOAuthFlowId() {
        return oasOAuthFlowId;
    }

    public void setOasOAuthFlowId(BigInteger oasOAuthFlowId) {
        this.oasOAuthFlowId = oasOAuthFlowId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getRefreshUrl() {
        return refreshUrl;
    }

    public void setRefreshUrl(String refreshUrl) {
        this.refreshUrl = refreshUrl;
    }

    public String getDeviceAuthorizationUrl() {
        return deviceAuthorizationUrl;
    }

    public void setDeviceAuthorizationUrl(String deviceAuthorizationUrl) {
        this.deviceAuthorizationUrl = deviceAuthorizationUrl;
    }

    public List<OasOAuthScope> getScopes() {
        return scopes;
    }

    public void setScopes(List<OasOAuthScope> scopes) {
        this.scopes = scopes;
    }
}
