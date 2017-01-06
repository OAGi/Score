package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "bdt_pri_restri")
public class BusinessDataTypePrimitiveRestriction implements Serializable {

    public static final String SEQUENCE_NAME = "BDT_PRI_RESTRI_ID_SEQ";

    @Id
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.oagi.srt.repository.support.jpa.ByDialectIdentifierGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1")
            }
    )
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.AUTO)
    private long bdtPriRestriId;

    @Column(nullable = false)
    private long bdtId;

    @Column
    private Long cdtAwdPriXpsTypeMapId;

    @Column
    private Long codeListId;

    @Column
    private Long agencyIdListId;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    public BusinessDataTypePrimitiveRestriction() {
    }

    public BusinessDataTypePrimitiveRestriction(int bdtPriRestriId) {
        this.bdtPriRestriId = bdtPriRestriId;
    }

    public long getBdtPriRestriId() {
        return bdtPriRestriId;
    }

    public void setBdtPriRestriId(long bdtPriRestriId) {
        this.bdtPriRestriId = bdtPriRestriId;
    }

    public long getBdtId() {
        return bdtId;
    }

    public void setBdtId(long bdtId) {
        this.bdtId = bdtId;
    }

    public long getCdtAwdPriXpsTypeMapId() {
        return (cdtAwdPriXpsTypeMapId == null) ? 0L : cdtAwdPriXpsTypeMapId;
    }

    public void setCdtAwdPriXpsTypeMapId(long cdtAwdPriXpsTypeMapId) {
        if (cdtAwdPriXpsTypeMapId > 0) {
            this.cdtAwdPriXpsTypeMapId = cdtAwdPriXpsTypeMapId;
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

        BusinessDataTypePrimitiveRestriction that = (BusinessDataTypePrimitiveRestriction) o;

        if (bdtPriRestriId != 0L && bdtPriRestriId == that.bdtPriRestriId) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (bdtPriRestriId ^ (bdtPriRestriId >>> 32));
        result = 31 * result + (int) (bdtId ^ (bdtId >>> 32));
        result = 31 * result + (cdtAwdPriXpsTypeMapId != null ? cdtAwdPriXpsTypeMapId.hashCode() : 0);
        result = 31 * result + (codeListId != null ? codeListId.hashCode() : 0);
        result = 31 * result + (agencyIdListId != null ? agencyIdListId.hashCode() : 0);
        result = 31 * result + (isDefault ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BusinessDataTypePrimitiveRestriction{" +
                "bdtPriRestriId=" + bdtPriRestriId +
                ", bdtId=" + bdtId +
                ", cdtAwdPriXpsTypeMapId=" + cdtAwdPriXpsTypeMapId +
                ", codeListId=" + codeListId +
                ", agencyIdListId=" + agencyIdListId +
                ", isDefault=" + isDefault +
                '}';
    }
}
