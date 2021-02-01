package org.oagi.score.repo.component.code_list_value;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpdateCodeListValueRepositoryRequest extends RepositoryRequest {

    private final BigInteger codeListValueManifestId;

    private String value;
    private String name;
    private String definition;
    private String definitionSource;

    private boolean used;
    private boolean locked;
    private boolean extension;

    public UpdateCodeListValueRepositoryRequest(AuthenticatedPrincipal user,
                                                BigInteger codeListValueManifestId) {
        super(user);
        this.codeListValueManifestId = codeListValueManifestId;
    }

    public UpdateCodeListValueRepositoryRequest(AuthenticatedPrincipal user,
                                                LocalDateTime localDateTime,
                                                BigInteger codeListValueManifestId) {
        super(user, localDateTime);
        this.codeListValueManifestId = codeListValueManifestId;
    }

    public BigInteger getCodeListValueManifestId() {
        return codeListValueManifestId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isExtension() {
        return extension;
    }

    public void setExtension(boolean extension) {
        this.extension = extension;
    }
}
