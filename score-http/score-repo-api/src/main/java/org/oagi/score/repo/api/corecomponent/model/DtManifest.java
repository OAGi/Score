package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.corecomponent.CcManifest;

import java.io.Serializable;
import java.math.BigInteger;

public class DtManifest implements CcManifest, Serializable {

    private BigInteger dtManifestId;

    private BigInteger releaseId;

    private BigInteger dtId;

    private BigInteger basedDtManifestId;

    private boolean conflict;

    private BigInteger logId;

    private BigInteger prevDtManifestId;

    private BigInteger nextDtManifestId;

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }

    public void setDtManifestId(BigInteger dtManifestId) {
        this.dtManifestId = dtManifestId;
    }

    @Override
    public BigInteger getManifestId() {
        return getDtManifestId();
    }

    @Override
    public BigInteger getReleaseId() {
        return releaseId;
    }

    @Override
    public BigInteger getBasedCcId() {
        return getDtId();
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public BigInteger getDtId() {
        return dtId;
    }

    public void setDtId(BigInteger dtId) {
        this.dtId = dtId;
    }

    public BigInteger getBasedDtManifestId() {
        return basedDtManifestId;
    }

    public void setBasedDtManifestId(BigInteger basedDtManifestId) {
        this.basedDtManifestId = basedDtManifestId;
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

    public BigInteger getPrevDtManifestId() {
        return prevDtManifestId;
    }

    public void setPrevDtManifestId(BigInteger prevDtManifestId) {
        this.prevDtManifestId = prevDtManifestId;
    }

    public BigInteger getNextDtManifestId() {
        return nextDtManifestId;
    }

    public void setNextDtManifestId(BigInteger nextDtManifestId) {
        this.nextDtManifestId = nextDtManifestId;
    }
}
