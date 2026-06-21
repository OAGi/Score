package org.oagi.score.gateway.http.api.oas_management.model;

import java.math.BigInteger;

/**
 * Issue #1729: one entry of an OpenAPI OAuth Flow Object's {@code scopes} map (name -&gt; description).
 */
public class OasOAuthScope {
    private BigInteger oasOAuthScopeId;
    private String guid;
    private String scopeName;
    private String description;

    public OasOAuthScope() {
    }

    public BigInteger getOasOAuthScopeId() {
        return oasOAuthScopeId;
    }

    public void setOasOAuthScopeId(BigInteger oasOAuthScopeId) {
        this.oasOAuthScopeId = oasOAuthScopeId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getScopeName() {
        return scopeName;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
