package org.oagi.score.gateway.http.api.oas_management.model;

import java.math.BigInteger;
import java.util.List;

/**
 * Issue #1729: a single named OpenAPI Security Scheme configured at the OpenAPI document level.
 *
 * <p>Backend currently targets OpenAPI 3.0.3, so only the {@code apiKey}, {@code http} and
 * {@code oauth2} types are exercised. {@code oauth2} (or a {@code null}/blank type) means
 * "keep the legacy default OAuth2 authorizationCode scheme" and is NOT persisted as a row.</p>
 */
public class OasSecurityScheme {
    private BigInteger oasSecuritySchemeId;
    private String guid;
    // apiKey | http | oauth2 | openIdConnect
    private String type;
    // The components.securitySchemes map key (e.g. OAuth2, ApiKeyAuth, BearerAuth). Derived when blank.
    private String schemeName;
    private String description;
    // type=apiKey
    private String apiKeyName;
    private String apiKeyIn;
    // type=http
    private String httpScheme;
    private String bearerFormat;
    // type=openIdConnect
    private String openIdConnectUrl;
    // type=oauth2 -- the OAuth Flows Object (multiple flows, each with its own scopes)
    private List<OasOAuthFlow> flows;

    public OasSecurityScheme() {
    }

    public BigInteger getOasSecuritySchemeId() {
        return oasSecuritySchemeId;
    }

    public void setOasSecuritySchemeId(BigInteger oasSecuritySchemeId) {
        this.oasSecuritySchemeId = oasSecuritySchemeId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApiKeyName() {
        return apiKeyName;
    }

    public void setApiKeyName(String apiKeyName) {
        this.apiKeyName = apiKeyName;
    }

    public String getApiKeyIn() {
        return apiKeyIn;
    }

    public void setApiKeyIn(String apiKeyIn) {
        this.apiKeyIn = apiKeyIn;
    }

    public String getHttpScheme() {
        return httpScheme;
    }

    public void setHttpScheme(String httpScheme) {
        this.httpScheme = httpScheme;
    }

    public String getBearerFormat() {
        return bearerFormat;
    }

    public void setBearerFormat(String bearerFormat) {
        this.bearerFormat = bearerFormat;
    }

    public String getOpenIdConnectUrl() {
        return openIdConnectUrl;
    }

    public void setOpenIdConnectUrl(String openIdConnectUrl) {
        this.openIdConnectUrl = openIdConnectUrl;
    }

    public List<OasOAuthFlow> getFlows() {
        return flows;
    }

    public void setFlows(List<OasOAuthFlow> flows) {
        this.flows = flows;
    }

    @Override
    public String toString() {
        return "OasSecurityScheme{" +
                "type='" + type + '\'' +
                ", schemeName='" + schemeName + '\'' +
                ", apiKeyName='" + apiKeyName + '\'' +
                ", apiKeyIn='" + apiKeyIn + '\'' +
                ", httpScheme='" + httpScheme + '\'' +
                ", bearerFormat='" + bearerFormat + '\'' +
                ", openIdConnectUrl='" + openIdConnectUrl + '\'' +
                '}';
    }
}
