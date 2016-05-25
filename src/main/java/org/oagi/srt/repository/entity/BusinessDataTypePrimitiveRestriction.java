package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "bdt_pri_restri")
public class BusinessDataTypePrimitiveRestriction implements Serializable {

    @Id
    @GeneratedValue(generator = "BDT_PRI_RESTRI_ID_SEQ", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "BDT_PRI_RESTRI_ID_SEQ", sequenceName = "BDT_PRI_RESTRI_ID_SEQ", allocationSize = 1)
    private int bdtPriRestriId;

    @Column(nullable = false)
    private int bdtId;

    @Column
    private Integer cdtAwdPriXpsTypeMapId;

    @Column
    private Integer codeListId;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column
    private Integer agencyIdListId;

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
        return (cdtAwdPriXpsTypeMapId == null) ? 0 : cdtAwdPriXpsTypeMapId;
    }

    public void setCdtAwdPriXpsTypeMapId(int cdtAwdPriXpsTypeMapId) {
        this.cdtAwdPriXpsTypeMapId = cdtAwdPriXpsTypeMapId;
    }

    public int getCodeListId() {
        return (codeListId == null) ? 0 : codeListId;
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
        return (agencyIdListId == null) ? 0 : agencyIdListId;
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
