package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "bdt_sc_pri_restri")
public class BusinessDataTypeSupplementaryComponentPrimitiveRestriction implements Serializable {

    public static final String SEQUENCE_NAME = "BDT_SC_PRI_RESTRI_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
            }
    )
    private int bdtScPriRestriId;

    @Column(nullable = false)
    private int bdtScId;

    @Column
    private Integer cdtScAwdPriXpsTypeMapId;

    @Column
    private Integer codeListId;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column
    private Integer agencyIdListId;

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
        return (cdtScAwdPriXpsTypeMapId == null) ? 0 : cdtScAwdPriXpsTypeMapId;
    }

    public void setCdtScAwdPriXpsTypeMapId(int cdtScAwdPriXpsTypeMapId) {
        if (cdtScAwdPriXpsTypeMapId > 0) {
            this.cdtScAwdPriXpsTypeMapId = cdtScAwdPriXpsTypeMapId;
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
