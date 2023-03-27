package org.oagi.score.repo.component.acc;

import org.oagi.score.data.RepositoryRequest;
import org.oagi.score.gateway.http.api.cc_management.data.CcACCType;
import org.oagi.score.service.common.data.OagisComponentType;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class CreateAccRepositoryRequest extends RepositoryRequest {

    private final BigInteger releaseId;

    private String initialObjectClassTerm = "Object Class Term";
    private OagisComponentType initialComponentType = OagisComponentType.Semantics;
    private CcACCType initialType = CcACCType.Default;
    private String initialDefinition;
    private BigInteger basedAccManifestId;
    private BigInteger namespaceId;
    private String tag;

    public CreateAccRepositoryRequest(AuthenticatedPrincipal user, BigInteger releaseId) {
        super(user);
        this.releaseId = releaseId;
    }

    public CreateAccRepositoryRequest(AuthenticatedPrincipal user,
                                      LocalDateTime localDateTime,
                                      BigInteger releaseId) {
        super(user, localDateTime);
        this.releaseId = releaseId;
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public String getInitialObjectClassTerm() {
        return initialObjectClassTerm;
    }

    public void setInitialObjectClassTerm(String initialObjectClassTerm) {
        this.initialObjectClassTerm = initialObjectClassTerm;
    }

    public OagisComponentType getInitialComponentType() {
        return initialComponentType;
    }

    public void setInitialComponentType(OagisComponentType initialComponentType) {
        this.initialComponentType = initialComponentType;
    }

    public String getInitialDefinition() {
        return initialDefinition;
    }

    public void setInitialDefinition(String initialDefinition) {
        this.initialDefinition = initialDefinition;
    }

    public BigInteger getBasedAccManifestId() {
        return basedAccManifestId;
    }

    public void setBasedAccManifestId(BigInteger basedAccManifestId) {
        this.basedAccManifestId = basedAccManifestId;
    }

    public BigInteger getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(BigInteger namespaceId) {
        this.namespaceId = namespaceId;
    }

    public CcACCType getInitialType() {
        return initialType;
    }

    public void setInitialType(CcACCType initialType) {
        this.initialType = initialType;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
