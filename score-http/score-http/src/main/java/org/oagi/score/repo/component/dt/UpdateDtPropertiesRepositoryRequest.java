package org.oagi.score.repo.component.dt;

import org.oagi.score.data.RepositoryRequest;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcBdtPriRestri;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

public class UpdateDtPropertiesRepositoryRequest extends RepositoryRequest {

    private final BigInteger dtManifestId;

    private String qualifier;
    private BigInteger facetMinLength;
    private BigInteger facetMaxLength;
    private String facetPattern;
    private String sixDigitId;
    private String contentComponentDefinition;
    private String definition;
    private String definitionSource;
    private boolean deprecated;
    private BigInteger namespaceId;
    private List<CcBdtPriRestri> bdtPriRestriList;

    public UpdateDtPropertiesRepositoryRequest(AuthenticatedPrincipal user,
                                               BigInteger dtManifestId) {
        super(user);
        this.dtManifestId = dtManifestId;
    }

    public UpdateDtPropertiesRepositoryRequest(AuthenticatedPrincipal user,
                                               LocalDateTime localDateTime,
                                               BigInteger dtManifestId) {
        super(user, localDateTime);
        this.dtManifestId = dtManifestId;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        if (StringUtils.hasLength(qualifier)) {
            this.qualifier = qualifier;
        }
    }

    public BigInteger getFacetMinLength() {
        return facetMinLength;
    }

    public void setFacetMinLength(BigInteger facetMinLength) {
        this.facetMinLength = facetMinLength;
    }

    public BigInteger getFacetMaxLength() {
        return facetMaxLength;
    }

    public void setFacetMaxLength(BigInteger facetMaxLength) {
        this.facetMaxLength = facetMaxLength;
    }

    public String getFacetPattern() {
        return facetPattern;
    }

    public void setFacetPattern(String facetPattern) {
        if (StringUtils.hasLength(facetPattern)) {
            this.facetPattern = facetPattern;
        }
    }

    public String getSixDigitId() {
        return sixDigitId;
    }

    public void setSixDigitId(String sixDigitId) {
        this.sixDigitId = sixDigitId;
    }

    public String getContentComponentDefinition() {
        return contentComponentDefinition;
    }

    public void setContentComponentDefinition(String contentComponentDefinition) {
        if (StringUtils.hasLength(contentComponentDefinition)) {
            this.contentComponentDefinition = contentComponentDefinition;
        }
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        if (StringUtils.hasLength(definition)) {
            this.definition = definition;
        }
    }

    public String getDefinitionSource() {
        return definitionSource;
    }

    public void setDefinitionSource(String definitionSource) {
        if (StringUtils.hasLength(definitionSource)) {
            this.definitionSource = definitionSource;
        }
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public BigInteger getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(BigInteger namespaceId) {
        this.namespaceId = namespaceId;
    }

    public List<CcBdtPriRestri> getBdtPriRestriList() {
        return bdtPriRestriList;
    }

    public void setBdtPriRestriList(List<CcBdtPriRestri> bdtPriRestriList) {
        this.bdtPriRestriList = bdtPriRestriList;
    }
}
