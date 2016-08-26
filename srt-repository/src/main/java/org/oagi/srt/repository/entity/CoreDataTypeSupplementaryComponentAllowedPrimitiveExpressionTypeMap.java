package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cdt_sc_awd_pri_xps_type_map")
public class CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap implements Serializable {

    public static final String SEQUENCE_NAME = "CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ";

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
    private long cdtScAwdPriXpsTypeMapId;

    @Column(nullable = false)
    private long cdtScAwdPriId;

    @Column(nullable = false)
    private long xbtId;

    public long getCdtScAwdPriXpsTypeMapId() {
        return cdtScAwdPriXpsTypeMapId;
    }

    public void setCdtScAwdPriXpsTypeMapId(long cdtScAwdPriXpsTypeMapId) {
        this.cdtScAwdPriXpsTypeMapId = cdtScAwdPriXpsTypeMapId;
    }

    public long getCdtScAwdPriId() {
        return cdtScAwdPriId;
    }

    public void setCdtScAwdPriId(long cdtScAwdPriId) {
        this.cdtScAwdPriId = cdtScAwdPriId;
    }

    public long getXbtId() {
        return xbtId;
    }

    public void setXbtId(long xbtId) {
        this.xbtId = xbtId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap that = (CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap) o;

        if (cdtScAwdPriXpsTypeMapId != that.cdtScAwdPriXpsTypeMapId) return false;
        if (cdtScAwdPriId != that.cdtScAwdPriId) return false;
        return xbtId == that.xbtId;

    }

    @Override
    public int hashCode() {
        int result = (int) (cdtScAwdPriXpsTypeMapId ^ (cdtScAwdPriXpsTypeMapId >>> 32));
        result = 31 * result + (int) (cdtScAwdPriId ^ (cdtScAwdPriId >>> 32));
        result = 31 * result + (int) (xbtId ^ (xbtId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap{" +
                "cdtScAwdPriXpsTypeMapId=" + cdtScAwdPriXpsTypeMapId +
                ", cdtScAwdPriId=" + cdtScAwdPriId +
                ", xbtId=" + xbtId +
                '}';
    }
}
