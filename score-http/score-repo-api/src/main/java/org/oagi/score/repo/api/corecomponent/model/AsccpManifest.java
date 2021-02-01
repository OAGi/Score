package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.corecomponent.CcManifest;

import java.io.Serializable;
import java.math.BigInteger;

public class AsccpManifest implements CcManifest, Serializable {

    private BigInteger asccpManifestId;

    private BigInteger releaseId;

    private BigInteger asccpId;

    private BigInteger roleOfAccManifestId;

    private boolean conflict;

    private BigInteger logId;

    private BigInteger prevAsccpManifestId;

    private BigInteger nextAsccpManifestId;

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }

    public void setAsccpManifestId(BigInteger asccpManifestId) {
        this.asccpManifestId = asccpManifestId;
    }

    @Override
    public BigInteger getManifestId() {
        return getAsccpManifestId();
    }

    @Override
    public BigInteger getReleaseId() {
        return releaseId;
    }

    @Override
    public BigInteger getBasedCcId() {
        return getAsccpId();
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public BigInteger getAsccpId() {
        return asccpId;
    }

    public void setAsccpId(BigInteger asccpId) {
        this.asccpId = asccpId;
    }

    public BigInteger getRoleOfAccManifestId() {
        return roleOfAccManifestId;
    }

    public void setRoleOfAccManifestId(BigInteger roleOfAccManifestId) {
        this.roleOfAccManifestId = roleOfAccManifestId;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public BigInteger getLogId() {
        return logId;
    }

    public void setLogId(BigInteger logId) {
        this.logId = logId;
    }

    public BigInteger getPrevAsccpManifestId() {
        return prevAsccpManifestId;
    }

    public void setPrevAsccpManifestId(BigInteger prevAsccpManifestId) {
        this.prevAsccpManifestId = prevAsccpManifestId;
    }

    public BigInteger getNextAsccpManifestId() {
        return nextAsccpManifestId;
    }

    public void setNextAsccpManifestId(BigInteger nextAsccpManifestId) {
        this.nextAsccpManifestId = nextAsccpManifestId;
    }
}
