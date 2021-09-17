package org.oagi.score.repo.api.agency.model;

import org.oagi.score.repo.api.corecomponent.CcManifest;

import java.io.Serializable;
import java.math.BigInteger;

public class AgencyIdListValueManifest implements CcManifest, Serializable {

    private BigInteger agencyIdListValueManifestId;

    private BigInteger releaseId;

    private BigInteger agencyIdListValueId;

    private BigInteger agencyIdListManifestId;

    private boolean conflict;

    private BigInteger prevAgencyIdListManifestId;

    private BigInteger nextAgencyIdListManifestId;

    @Override
    public BigInteger getManifestId() {
        return agencyIdListValueManifestId;
    }

    @Override
    public BigInteger getBasedCcId() {
        return agencyIdListManifestId;
    }

    public BigInteger getAgencyIdListValueManifestId() {
        return agencyIdListValueManifestId;
    }

    public void setAgencyIdListValueManifestId(BigInteger agencyIdListValueManifestId) {
        this.agencyIdListValueManifestId = agencyIdListValueManifestId;
    }

    @Override
    public BigInteger getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public BigInteger getAgencyIdListValueId() {
        return agencyIdListValueId;
    }

    public void setAgencyIdListValueId(BigInteger agencyIdListValueId) {
        this.agencyIdListValueId = agencyIdListValueId;
    }

    public BigInteger getAgencyIdListManifestId() {
        return agencyIdListManifestId;
    }

    public void setAgencyIdListManifestId(BigInteger agencyIdListManifestId) {
        this.agencyIdListManifestId = agencyIdListManifestId;
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
