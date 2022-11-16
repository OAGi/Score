package org.oagi.score.repo.api.corecomponent.model;

import java.io.Serializable;
import java.math.BigInteger;

public class BdtPriRestri implements Serializable {

    private BigInteger bdtPriRestriId;
    private BigInteger bdtManifestId;
    private BigInteger xbtId;
    private String xbtName;
    private BigInteger cdtAwdPriXpsTypeMapId;
    private BigInteger codeListManifestId;
    private BigInteger agencyIdListManifestId;
    private boolean isDefault;

    public BigInteger getBdtPriRestriId() {
        return bdtPriRestriId;
    }

    public void setBdtPriRestriId(BigInteger bdtPriRestriId) {
        this.bdtPriRestriId = bdtPriRestriId;
    }

    public BigInteger getBdtManifestId() {
        return bdtManifestId;
    }

    public void setBdtManifestId(BigInteger bdtManifestId) {
        this.bdtManifestId = bdtManifestId;
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

    public BigInteger getCdtAwdPriXpsTypeMapId() {
        return cdtAwdPriXpsTypeMapId;
    }

    public void setCdtAwdPriXpsTypeMapId(BigInteger cdtAwdPriXpsTypeMapId) {
        this.cdtAwdPriXpsTypeMapId = cdtAwdPriXpsTypeMapId;
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
