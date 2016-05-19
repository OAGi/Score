package org.oagi.srt.repository.entity;

import java.io.Serializable;

public class BusinessDataTypeSupplementaryComponentPrimitiveRestriction implements Serializable {

    private int bdtScPriRestriId;
    private int bdtScId;
    private int cdtScAwdPriXpsTypeMapId;
    private int codeListId;
    private boolean isDefault;
    private int agencyIdListId;

    public int getBdtScPriRestriId() {
        return bdtScPriRestriId;
    }

    public void setBdtScPriRestriId(int bdtScPriRestriId) {
        this.bdtScPriRestriId = bdtScPriRestriId;
    }

    public int getBdtScId() {
        return bdtScId;
    }

    public void setBdtScId(int bdtScId) {
        this.bdtScId = bdtScId;
    }

    public int getCdtScAwdPriXpsTypeMapId() {
        return cdtScAwdPriXpsTypeMapId;
    }

    public void setCdtScAwdPriXpsTypeMapId(int cdtScAwdPriXpsTypeMapId) {
        this.cdtScAwdPriXpsTypeMapId = cdtScAwdPriXpsTypeMapId;
    }

    public int getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(int codeListId) {
        this.codeListId = codeListId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public int getAgencyIdListId() {
        return agencyIdListId;
    }

    public void setAgencyIdListId(int agencyIdListId) {
        this.agencyIdListId = agencyIdListId;
    }

    @Override
    public String toString() {
        return "BusinessDataTypeSupplementaryComponentPrimitiveRestriction{" +
                "bdtScPriRestriId=" + bdtScPriRestriId +
                ", bdtScId=" + bdtScId +
                ", cdtScAwdPriXpsTypeMapId=" + cdtScAwdPriXpsTypeMapId +
                ", codeListId=" + codeListId +
                ", isDefault=" + isDefault +
                ", agencyIdListId=" + agencyIdListId +
                '}';
    }
}
