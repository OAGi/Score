package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "bdt_sc_pri_restri")
public class BusinessDataTypeSupplementaryComponentPrimitiveRestriction implements Serializable {

    public static final String SEQUENCE_NAME = "BDT_SC_PRI_RESTRI_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long bdtScPriRestriId;

    @Column(nullable = false)
    private long bdtScId;

    @Column
    private Long cdtScAwdPriXpsTypeMapId;

    @Column
    private Long codeListId;

    @Column
    private Long agencyIdListId;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    public long getBdtScPriRestriId() {
        return bdtScPriRestriId;
    }

    public void setBdtScPriRestriId(long bdtScPriRestriId) {
        this.bdtScPriRestriId = bdtScPriRestriId;
    }

    public long getBdtScId() {
        return bdtScId;
    }

    public void setBdtScId(long bdtScId) {
        this.bdtScId = bdtScId;
    }

    public long getCdtScAwdPriXpsTypeMapId() {
        return (cdtScAwdPriXpsTypeMapId == null) ? 0L : cdtScAwdPriXpsTypeMapId;
    }

    public void setCdtScAwdPriXpsTypeMapId(long cdtScAwdPriXpsTypeMapId) {
        if (cdtScAwdPriXpsTypeMapId > 0) {
            this.cdtScAwdPriXpsTypeMapId = cdtScAwdPriXpsTypeMapId;
        }
    }

    public long getCodeListId() {
        return (codeListId == null) ? 0L : codeListId;
    }

    public void setCodeListId(long codeListId) {
        if (codeListId > 0) {
            this.codeListId = codeListId;
        }
    }

    public long getAgencyIdListId() {
        return (agencyIdListId == null) ? 0L : agencyIdListId;
    }

    public void setAgencyIdListId(long agencyIdListId) {
        if (agencyIdListId > 0) {
            this.agencyIdListId = agencyIdListId;
        }
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BusinessDataTypeSupplementaryComponentPrimitiveRestriction that = (BusinessDataTypeSupplementaryComponentPrimitiveRestriction) o;

        if (bdtScPriRestriId != that.bdtScPriRestriId) return false;
        if (bdtScId != that.bdtScId) return false;
        if (isDefault != that.isDefault) return false;
        if (cdtScAwdPriXpsTypeMapId != null ? !cdtScAwdPriXpsTypeMapId.equals(that.cdtScAwdPriXpsTypeMapId) : that.cdtScAwdPriXpsTypeMapId != null)
            return false;
        if (codeListId != null ? !codeListId.equals(that.codeListId) : that.codeListId != null) return false;
        return agencyIdListId != null ? agencyIdListId.equals(that.agencyIdListId) : that.agencyIdListId == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (bdtScPriRestriId ^ (bdtScPriRestriId >>> 32));
        result = 31 * result + (int) (bdtScId ^ (bdtScId >>> 32));
        result = 31 * result + (cdtScAwdPriXpsTypeMapId != null ? cdtScAwdPriXpsTypeMapId.hashCode() : 0);
        result = 31 * result + (codeListId != null ? codeListId.hashCode() : 0);
        result = 31 * result + (agencyIdListId != null ? agencyIdListId.hashCode() : 0);
        result = 31 * result + (isDefault ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BusinessDataTypeSupplementaryComponentPrimitiveRestriction{" +
                "bdtScPriRestriId=" + bdtScPriRestriId +
                ", bdtScId=" + bdtScId +
                ", cdtScAwdPriXpsTypeMapId=" + cdtScAwdPriXpsTypeMapId +
                ", codeListId=" + codeListId +
                ", agencyIdListId=" + agencyIdListId +
                ", isDefault=" + isDefault +
                '}';
    }
}
