package org.oagi.score.repo.api.corecomponent.model;

import java.io.Serializable;
import java.math.BigInteger;

public class BdtPriRestri implements Serializable {

    private BigInteger bdtPriRestriId;
    private BigInteger bdtId;
    private BigInteger cdtAwdPriXpsTypeMapId;
    private BigInteger codeListId;
    private BigInteger agencyIdListId;
    private boolean isDefault;

    public BigInteger getBdtPriRestriId() {
        return bdtPriRestriId;
    }

    public void setBdtPriRestriId(BigInteger bdtPriRestriId) {
        this.bdtPriRestriId = bdtPriRestriId;
    }

    public BigInteger getBdtId() {
        return bdtId;
    }

    public void setBdtId(BigInteger bdtId) {
        this.bdtId = bdtId;
    }

    public BigInteger getCdtAwdPriXpsTypeMapId() {
        return cdtAwdPriXpsTypeMapId;
    }

    public void setCdtAwdPriXpsTypeMapId(BigInteger cdtAwdPriXpsTypeMapId) {
        this.cdtAwdPriXpsTypeMapId = cdtAwdPriXpsTypeMapId;
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
