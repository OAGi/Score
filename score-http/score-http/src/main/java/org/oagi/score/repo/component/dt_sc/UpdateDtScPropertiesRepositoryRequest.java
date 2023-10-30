package org.oagi.score.repo.component.dt_sc;

import org.oagi.score.data.RepositoryRequest;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcBdtScPriRestri;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

public class UpdateDtScPropertiesRepositoryRequest extends RepositoryRequest {

    private final BigInteger dtScManifestId;

    private String propertyTerm;
    private String representationTerm;
    private String defaultValue;
    private String fixedValue;
    private Integer cardinalityMin;
    private Integer cardinalityMax;
    private String definition;
    private String definitionSource;
    private Boolean deprecated;
    private List<CcBdtScPriRestri> ccBdtScPriRestriList;

    public UpdateDtScPropertiesRepositoryRequest(AuthenticatedPrincipal user,
                                                 BigInteger dtScManifestId) {
        super(user);
        this.dtScManifestId = dtScManifestId;
    }

    public UpdateDtScPropertiesRepositoryRequest(AuthenticatedPrincipal user,
                                                 LocalDateTime localDateTime,
                                                 BigInteger dtScManifestId) {
        super(user, localDateTime);
        this.dtScManifestId = dtScManifestId;
    }

    public BigInteger getDtScManifestId() {
        return dtScManifestId;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDefinitionSource() {
        return definitionSource;
    }

    public void setDefinitionSource(String definitionSource) {
        this.definitionSource = definitionSource;
    }

    public Boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Integer getCardinalityMin() {
        return cardinalityMin;
    }

    public void setCardinalityMin(Integer cardinalityMin) {
        this.cardinalityMin = cardinalityMin;
    }

    public Integer getCardinalityMax() {
        return cardinalityMax;
    }

    public void setCardinalityMax(Integer cardinalityMax) {
        this.cardinalityMax = cardinalityMax;
    }

    public List<CcBdtScPriRestri> getCcBdtScPriResriList() {
        return ccBdtScPriRestriList;
    }

    public void setCcBdtScPriResriList(List<CcBdtScPriRestri> ccBdtScPriRestriList) {
        this.ccBdtScPriRestriList = ccBdtScPriRestriList;
    }

    public String getRepresentationTerm() {
        return representationTerm;
    }

    public void setRepresentationTerm(String representationTerm) {
        this.representationTerm = representationTerm;
    }
}
