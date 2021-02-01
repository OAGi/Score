package org.oagi.score.repo.component.asccp;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpdateAsccpPropertiesRepositoryRequest extends RepositoryRequest {

    private final BigInteger asccpManifestId;

    private String propertyTerm;
    private String definition;
    private String definitionSource;
    private boolean reusable;
    private boolean deprecated;
    private boolean nillable;
    private BigInteger namespaceId;

    public UpdateAsccpPropertiesRepositoryRequest(AuthenticatedPrincipal user,
                                                  BigInteger asccpManifestId) {
        super(user);
        this.asccpManifestId = asccpManifestId;
    }

    public UpdateAsccpPropertiesRepositoryRequest(AuthenticatedPrincipal user,
                                                  LocalDateTime localDateTime,
                                                  BigInteger asccpManifestId) {
        super(user, localDateTime);
        this.asccpManifestId = asccpManifestId;
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
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

    public boolean isReusable() {
        return reusable;
    }

    public void setReusable(boolean reusable) {
        this.reusable = reusable;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public BigInteger getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(BigInteger namespaceId) {
        this.namespaceId = namespaceId;
    }
}
