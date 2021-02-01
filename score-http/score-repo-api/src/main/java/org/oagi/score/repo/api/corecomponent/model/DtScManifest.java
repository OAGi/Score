package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.corecomponent.CcManifest;

import java.io.Serializable;
import java.math.BigInteger;

public class DtScManifest implements CcManifest, Serializable {

    private BigInteger dtScManifestId;

    private BigInteger releaseId;

    private BigInteger dtScId;

    private BigInteger ownerDtManifestId;

    private boolean conflict;

    private BigInteger logId;

    private BigInteger prevDtScManifestId;

    private BigInteger nextDtScManifestId;

    public BigInteger getDtScManifestId() {
        return dtScManifestId;
    }

    public void setDtScManifestId(BigInteger dtScManifestId) {
        this.dtScManifestId = dtScManifestId;
    }

    @Override
    public BigInteger getManifestId() {
        return getDtScManifestId();
    }

    @Override
    public BigInteger getReleaseId() {
        return releaseId;
    }

    @Override
    public BigInteger getBasedCcId() {
        return getDtScId();
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public BigInteger getDtScId() {
        return dtScId;
    }

    public void setDtScId(BigInteger dtScId) {
        this.dtScId = dtScId;
    }

    public BigInteger getOwnerDtManifestId() {
        return ownerDtManifestId;
    }

    public void setOwnerDtManifestId(BigInteger ownerDtManifestId) {
        this.ownerDtManifestId = ownerDtManifestId;
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

    public BigInteger getPrevDtScManifestId() {
        return prevDtScManifestId;
    }

    public void setPrevDtScManifestId(BigInteger prevDtScManifestId) {
        this.prevDtScManifestId = prevDtScManifestId;
    }

    public BigInteger getNextDtScManifestId() {
        return nextDtScManifestId;
    }

    public void setNextDtScManifestId(BigInteger nextDtScManifestId) {
        this.nextDtScManifestId = nextDtScManifestId;
    }
}
