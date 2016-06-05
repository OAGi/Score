package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "bdt_pri_restri")
public class BusinessDataTypePrimitiveRestriction implements Serializable {

    public static final String SEQUENCE_NAME = "BDT_PRI_RESTRI_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.AUTO)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
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

    public BusinessDataTypePrimitiveRestriction() {}

    public BusinessDataTypePrimitiveRestriction(int bdtPriRestriId) {
        this.bdtPriRestriId = bdtPriRestriId;
    }

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
        if (cdtAwdPriXpsTypeMapId > 0) {
            this.cdtAwdPriXpsTypeMapId = cdtAwdPriXpsTypeMapId;
        }
    }

    public int getCodeListId() {
        return (codeListId == null) ? 0 : codeListId;
    }

    public void setCodeListId(int codeListId) {
        if (codeListId > 0) {
            this.codeListId = codeListId;
        }
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
        if (agencyIdListId > 0) {
            this.agencyIdListId = agencyIdListId;
        }
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
