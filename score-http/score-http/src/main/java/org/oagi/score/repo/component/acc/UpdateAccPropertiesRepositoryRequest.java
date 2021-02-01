package org.oagi.score.repo.component.acc;

import org.oagi.score.service.common.data.OagisComponentType;
import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpdateAccPropertiesRepositoryRequest extends RepositoryRequest {

    private final BigInteger accManifestId;

    private String objectClassTerm;
    private String definition;
    private String definitionSource;
    private OagisComponentType componentType;
    private boolean isAbstract;
    private boolean deprecated;
    private BigInteger namespaceId;

    public UpdateAccPropertiesRepositoryRequest(AuthenticatedPrincipal user,
                                                BigInteger accManifestId) {
        super(user);
        this.accManifestId = accManifestId;
    }

    public UpdateAccPropertiesRepositoryRequest(AuthenticatedPrincipal user,
                                                LocalDateTime localDateTime,
                                                BigInteger accManifestId) {
        super(user, localDateTime);
        this.accManifestId = accManifestId;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }

    public String getObjectClassTerm() {
        return objectClassTerm;
    }

    public void setObjectClassTerm(String objectClassTerm) {
        this.objectClassTerm = objectClassTerm;
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

    public OagisComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(OagisComponentType componentType) {
        this.componentType = componentType;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
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
}
