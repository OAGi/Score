package org.oagi.score.repo.api.corecomponent.model;

import java.io.Serializable;
import java.math.BigInteger;

public class BdtScPriRestri implements Serializable {

    private BigInteger bdtScPriRestriId;
    private BigInteger bdtScId;
    private BigInteger xbtId;
    private String xbtName;
    private BigInteger cdtScAwdPriXpsTypeMapId;
    private BigInteger codeListId;
    private BigInteger agencyIdListId;
    private boolean isDefault;

    public BigInteger getBdtScPriRestriId() {
        return bdtScPriRestriId;
    }

    public void setBdtScPriRestriId(BigInteger bdtScPriRestriId) {
        this.bdtScPriRestriId = bdtScPriRestriId;
    }

    public BigInteger getBdtScId() {
        return bdtScId;
    }

    public void setBdtScId(BigInteger bdtScId) {
        this.bdtScId = bdtScId;
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

    public BigInteger getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(BigInteger codeListId) {
        this.codeListId = codeListId;
    }

    public BigInteger getAgencyIdListId() {
        return agencyIdListId;
    }

    public void setAgencyIdListId(BigInteger agencyIdListId) {
        this.agencyIdListId = agencyIdListId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
