package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cdt_awd_pri_xps_type_map")
public class CoreDataTypeAllowedPrimitiveExpressionTypeMap implements Serializable {

    public static final String SEQUENCE_NAME = "CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME)
    private long cdtAwdPriXpsTypeMapId;

    @Column(nullable = false)
    private long cdtAwdPriId;

    @Column(nullable = false)
    private long xbtId;

    public long getCdtAwdPriXpsTypeMapId() {
        return cdtAwdPriXpsTypeMapId;
    }

    public void setCdtAwdPriXpsTypeMapId(long cdtAwdPriXpsTypeMapId) {
        this.cdtAwdPriXpsTypeMapId = cdtAwdPriXpsTypeMapId;
    }

    public long getCdtAwdPriId() {
        return cdtAwdPriId;
    }

    public void setCdtAwdPriId(long cdtAwdPriId) {
        this.cdtAwdPriId = cdtAwdPriId;
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

        CoreDataTypeAllowedPrimitiveExpressionTypeMap that = (CoreDataTypeAllowedPrimitiveExpressionTypeMap) o;

        if (cdtAwdPriXpsTypeMapId != that.cdtAwdPriXpsTypeMapId) return false;
        if (cdtAwdPriId != that.cdtAwdPriId) return false;
        return xbtId == that.xbtId;

    }

    @Override
    public int hashCode() {
        int result = (int) (cdtAwdPriXpsTypeMapId ^ (cdtAwdPriXpsTypeMapId >>> 32));
        result = 31 * result + (int) (cdtAwdPriId ^ (cdtAwdPriId >>> 32));
        result = 31 * result + (int) (xbtId ^ (xbtId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "CoreDataTypeAllowedPrimitiveExpressionTypeMap{" +
                "cdtAwdPriXpsTypeMapId=" + cdtAwdPriXpsTypeMapId +
                ", cdtAwdPriId=" + cdtAwdPriId +
                ", xbtId=" + xbtId +
                '}';
    }
}
