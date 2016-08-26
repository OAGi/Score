package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cdt_awd_pri")
public class CoreDataTypeAllowedPrimitive implements Serializable {

    public static final String SEQUENCE_NAME = "CDT_AWD_PRI_ID_SEQ";

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
    private long cdtAwdPriId;

    @Column(nullable = false)
    private long cdtId;

    @Column(nullable = false)
    private long cdtPriId;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    public long getCdtAwdPriId() {
        return cdtAwdPriId;
    }

    public void setCdtAwdPriId(long cdtAwdPriId) {
        this.cdtAwdPriId = cdtAwdPriId;
    }

    public long getCdtId() {
        return cdtId;
    }

    public void setCdtId(long cdtId) {
        this.cdtId = cdtId;
    }

    public long getCdtPriId() {
        return cdtPriId;
    }

    public void setCdtPriId(long cdtPriId) {
        this.cdtPriId = cdtPriId;
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

        CoreDataTypeAllowedPrimitive that = (CoreDataTypeAllowedPrimitive) o;

        if (cdtAwdPriId != that.cdtAwdPriId) return false;
        if (cdtId != that.cdtId) return false;
        if (cdtPriId != that.cdtPriId) return false;
        return isDefault == that.isDefault;

    }

    @Override
    public int hashCode() {
        int result = (int) (cdtAwdPriId ^ (cdtAwdPriId >>> 32));
        result = 31 * result + (int) (cdtId ^ (cdtId >>> 32));
        result = 31 * result + (int) (cdtPriId ^ (cdtPriId >>> 32));
        result = 31 * result + (isDefault ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CoreDataTypeAllowedPrimitive{" +
                "cdtAwdPriId=" + cdtAwdPriId +
                ", cdtId=" + cdtId +
                ", cdtPriId=" + cdtPriId +
                ", isDefault=" + isDefault +
                '}';
    }
}
