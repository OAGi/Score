package org.oagi.srt.repository.entity;

import java.io.Serializable;

public class BusinessDataTypePrimitiveRestriction implements Serializable {

    private int bdtPriRestriId;
    private int bdtId;
    private int cdtAwdPriXpsTypeMapId;
    private int codeListId;
    private boolean isDefault;
    private int agencyIdListId;

    public int getBdtPriRestriId() {
        return bdtPriRestriId;
    }

    public void setBdtPriRestriId(int bdtPriRestriId) {
        this.bdtPriRestriId = bdtPriRestriId;
    }

    public int getBdtId() {
        return bdtId;
    }

    public void setBdtId(int bdtId) {
        this.bdtId = bdtId;
    }

    public int getCdtAwdPriXpsTypeMapId() {
        return cdtAwdPriXpsTypeMapId;
    }

    public void setCdtAwdPriXpsTypeMapId(int cdtAwdPriXpsTypeMapId) {
        this.cdtAwdPriXpsTypeMapId = cdtAwdPriXpsTypeMapId;
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
        return "BusinessDataTypePrimitiveRestriction{" +
                "bdtPriRestriId=" + bdtPriRestriId +
                ", bdtId=" + bdtId +
                ", cdtAwdPriXpsTypeMapId=" + cdtAwdPriXpsTypeMapId +
                ", codeListId=" + codeListId +
                ", isDefault=" + isDefault +
                ", agencyIdListId=" + agencyIdListId +
                '}';
    }
}
