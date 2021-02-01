package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.corecomponent.CcManifest;

import java.io.Serializable;
import java.math.BigInteger;

public class BccpManifest implements CcManifest, Serializable {

    private BigInteger bccpManifestId;

    private BigInteger releaseId;

    private BigInteger bccpId;

    private BigInteger bdtManifestId;

    private boolean conflict;

    private BigInteger logId;

    private BigInteger prevBccpManifestId;

    private BigInteger nextBccpManifestId;

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }

    public void setBccpManifestId(BigInteger bccpManifestId) {
        this.bccpManifestId = bccpManifestId;
    }

    @Override
    public BigInteger getManifestId() {
        return getBccpManifestId();
    }

    @Override
    public BigInteger getReleaseId() {
        return releaseId;
    }

    @Override
    public BigInteger getBasedCcId() {
        return getBccpId();
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public BigInteger getBccpId() {
        return bccpId;
    }

    public void setBccpId(BigInteger bccpId) {
        this.bccpId = bccpId;
    }

    public BigInteger getBdtManifestId() {
        return bdtManifestId;
    }

    public void setBdtManifestId(BigInteger bdtManifestId) {
        this.bdtManifestId = bdtManifestId;
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

    public BigInteger getPrevBccpManifestId() {
        return prevBccpManifestId;
    }

    public void setPrevBccpManifestId(BigInteger prevBccpManifestId) {
        this.prevBccpManifestId = prevBccpManifestId;
    }

    public BigInteger getNextBccpManifestId() {
        return nextBccpManifestId;
    }

    public void setNextBccpManifestId(BigInteger nextBccpManifestId) {
        this.nextBccpManifestId = nextBccpManifestId;
    }
}
