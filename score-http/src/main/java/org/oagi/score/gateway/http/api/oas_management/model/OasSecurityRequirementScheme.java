package org.oagi.score.gateway.http.api.oas_management.model;

import java.util.List;

/**
 * Issue #1729: one scheme entry of a {@link OasSecurityRequirement} (an AND member of a Security
 * Requirement Object). {@code schemeName} references a components.securitySchemes key; {@code scopes}
 * are the required scope names (populated for oauth2/openIdConnect; empty for apiKey/http/mutualTLS).
 */
public class OasSecurityRequirementScheme {
    private String schemeName;
    private List<String> scopes;

    public OasSecurityRequirementScheme() {
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }
}
