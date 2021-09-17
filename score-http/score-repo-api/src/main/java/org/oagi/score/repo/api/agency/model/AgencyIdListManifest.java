package org.oagi.score.repo.api.agency.model;

import org.oagi.score.repo.api.corecomponent.CcManifest;
import org.oagi.score.repo.api.corecomponent.model.CcState;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.io.Serializable;
import java.math.BigInteger;

public class AgencyIdListManifest implements CcManifest, Serializable {

    private BigInteger agencyIdListManifestId;

    private BigInteger releaseId;

    private BigInteger agencyIdListId;

    private BigInteger basedAgencyIdListManifestId;

    private BigInteger logId;

    private boolean conflict;

    private BigInteger prevAgencyIdListManifestId;

    private BigInteger nextAgencyIdListManifestId;

    @Override
    public BigInteger getManifestId() {
        return agencyIdListManifestId;
    }

    @Override
    public BigInteger getBasedCcId() {
        return basedAgencyIdListManifestId;
    }

    public BigInteger getAgencyIdListManifestId() {
        return agencyIdListManifestId;
    }

    public void setAgencyIdListManifestId(BigInteger agencyIdListManifestId) {
        this.agencyIdListManifestId = agencyIdListManifestId;
    }

    @Override
    public BigInteger getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public BigInteger getAgencyIdListId() {
        return agencyIdListId;
    }

    public void setAgencyIdListId(BigInteger agencyIdListId) {
        this.agencyIdListId = agencyIdListId;
    }

    public BigInteger getBasedAgencyIdListManifestId() {
        return basedAgencyIdListManifestId;
    }

    public void setBasedAgencyIdListManifestId(BigInteger basedAgencyIdListManifestId) {
        this.basedAgencyIdListManifestId = basedAgencyIdListManifestId;
    }

    public BigInteger getLogId() {
        return logId;
    }

    public void setLogId(BigInteger logId) {
        this.logId = logId;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public BigInteger getPrevAgencyIdListManifestId() {
        return prevAgencyIdListManifestId;
    }

    public void setPrevAgencyIdListManifestId(BigInteger prevAgencyIdListManifestId) {
        this.prevAgencyIdListManifestId = prevAgencyIdListManifestId;
    }

    public BigInteger getNextAgencyIdListManifestId() {
        return nextAgencyIdListManifestId;
    }

    public void setNextAgencyIdListManifestId(BigInteger nextAgencyIdListManifestId) {
        this.nextAgencyIdListManifestId = nextAgencyIdListManifestId;
    }
}
