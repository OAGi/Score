package org.oagi.score.repo.component.asbiep;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpsertAsbiepRequest extends RepositoryRequest {

    private final BigInteger topLevelAsbiepId;
    private final AsbiepNode.Asbiep asbiep;

    private BigInteger roleOfAbieId;
    private BigInteger refTopLevelAsbiepId;
    private boolean refTopLevelAsbiepIdNull;

    public UpsertAsbiepRequest(AuthenticatedPrincipal user, LocalDateTime localDateTime,
                               BigInteger topLevelAsbiepId, AsbiepNode.Asbiep asbiep) {
        super(user, localDateTime);
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.asbiep = asbiep;
    }

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public AsbiepNode.Asbiep getAsbiep() {
        return asbiep;
    }

    public BigInteger getRoleOfAbieId() {
        return roleOfAbieId;
    }

    public void setRoleOfAbieId(BigInteger roleOfAbieId) {
        this.roleOfAbieId = roleOfAbieId;
    }

    public BigInteger getRefTopLevelAsbiepId() {
        return refTopLevelAsbiepId;
    }

    public void setRefTopLevelAsbiepId(BigInteger refTopLevelAsbiepId) {
        this.refTopLevelAsbiepId = refTopLevelAsbiepId;
    }

    public boolean isRefTopLevelAsbiepIdNull() {
        return refTopLevelAsbiepIdNull;
    }

    public void setRefTopLevelAsbiepIdNull(boolean refTopLevelAsbiepIdNull) {
        this.refTopLevelAsbiepIdNull = refTopLevelAsbiepIdNull;
    }
}
