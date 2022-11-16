package org.oagi.score.repo.api.corecomponent.model;

import java.io.Serializable;
import java.math.BigInteger;

public class BdtScPriRestri implements Serializable {

    private BigInteger bdtScPriRestriId;
    private BigInteger bdtScManifestId;
    private BigInteger xbtId;
    private String xbtName;
    private BigInteger cdtScAwdPriXpsTypeMapId;
    private BigInteger codeListManifestId;
    private BigInteger agencyIdListManifestId;
    private boolean isDefault;

    public BigInteger getBdtScPriRestriId() {
        return bdtScPriRestriId;
    }

    public void setBdtScPriRestriId(BigInteger bdtScPriRestriId) {
        this.bdtScPriRestriId = bdtScPriRestriId;
    }

    public BigInteger getBdtScManifestId() {
        return bdtScManifestId;
    }

    public void setBdtScManifestId(BigInteger bdtScManifestId) {
        this.bdtScManifestId = bdtScManifestId;
    }

    public BigInteger getXbtId() {
        return xbtId;
    }

    public void setXbtId(BigInteger xbtId) {
        this.xbtId = xbtId;
    }

    public String getXbtName() {
        return xbtName;
    }

    public void setXbtName(String xbtName) {
        this.xbtName = xbtName;
    }

    public BigInteger getCdtScAwdPriXpsTypeMapId() {
        return cdtScAwdPriXpsTypeMapId;
    }

    public void setCdtScAwdPriXpsTypeMapId(BigInteger cdtScAwdPriXpsTypeMapId) {
        this.cdtScAwdPriXpsTypeMapId = cdtScAwdPriXpsTypeMapId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }

    public void setCodeListManifestId(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getAgencyIdListManifestId() {
        return agencyIdListManifestId;
    }

    public void setAgencyIdListManifestId(BigInteger agencyIdListManifestId) {
        this.agencyIdListManifestId = agencyIdListManifestId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
