package org.oagi.score.repo.component.dt;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.util.List;

public class CreatePrimitiveRestrictionRepositoryRequest extends RepositoryRequest {

    private final BigInteger dtManifestId;
    private final BigInteger releaseId;

    private String primitive;
    private List<BigInteger> xbtManifestIdList;



    public CreatePrimitiveRestrictionRepositoryRequest(AuthenticatedPrincipal user,
                                                       BigInteger dtManifestId, BigInteger releaseId) {
        super(user);
        this.dtManifestId = dtManifestId;
        this.releaseId = releaseId;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public String getPrimitive() {
        return primitive;
    }

    public void setPrimitive(String primitive) {
        this.primitive = primitive;
    }

    public List<BigInteger> getXbtManifestIdList() {
        return xbtManifestIdList;
    }

    public void setXbtManifestIdList(List<BigInteger> xbtManifestIdList) {
        this.xbtManifestIdList = xbtManifestIdList;
    }
}
