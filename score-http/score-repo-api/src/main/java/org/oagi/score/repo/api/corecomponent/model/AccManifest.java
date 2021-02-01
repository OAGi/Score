package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.corecomponent.CcManifest;

import java.io.Serializable;
import java.math.BigInteger;

public class AccManifest implements CcManifest, Serializable {

    private BigInteger accManifestId;

    private BigInteger releaseId;

    private BigInteger accId;

    private BigInteger basedAccManifestId;

    private boolean conflict;

    private BigInteger logId;

    private BigInteger prevAccManifestId;

    private BigInteger nextAccManifestId;

    public BigInteger getAccManifestId() {
        return accManifestId;
    }

    public void setAccManifestId(BigInteger accManifestId) {
        this.accManifestId = accManifestId;
    }

    @Override
    public BigInteger getManifestId() {
        return getAccManifestId();
    }

    @Override
    public BigInteger getReleaseId() {
        return releaseId;
    }

    @Override
    public BigInteger getBasedCcId() {
        return getAccId();
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public BigInteger getAccId() {
        return accId;
    }

    public void setAccId(BigInteger accId) {
        this.accId = accId;
    }

    public BigInteger getBasedAccManifestId() {
        return basedAccManifestId;
    }

    public void setBasedAccManifestId(BigInteger basedAccManifestId) {
        this.basedAccManifestId = basedAccManifestId;
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

    public BigInteger getPrevAccManifestId() {
        return prevAccManifestId;
    }

    public void setPrevAccManifestId(BigInteger prevAccManifestId) {
        this.prevAccManifestId = prevAccManifestId;
    }

    public BigInteger getNextAccManifestId() {
        return nextAccManifestId;
    }

    public void setNextAccManifestId(BigInteger nextAccManifestId) {
        this.nextAccManifestId = nextAccManifestId;
    }
}
