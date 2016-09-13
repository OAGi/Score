package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cdt_sc_awd_pri")
public class CoreDataTypeSupplementaryComponentAllowedPrimitive implements Serializable {

    public static final String SEQUENCE_NAME = "CDT_SC_AWD_PRI_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long cdtScAwdPriId;

    @Column(nullable = false)
    private long cdtScId;

    @Column(nullable = false)
    private long cdtPriId;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    public long getCdtScAwdPriId() {
        return cdtScAwdPriId;
    }

    public void setCdtScAwdPriId(long cdtScAwdPriId) {
        this.cdtScAwdPriId = cdtScAwdPriId;
    }

    public long getCdtScId() {
        return cdtScId;
    }

    public void setCdtScId(long cdtScId) {
        this.cdtScId = cdtScId;
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

        CoreDataTypeSupplementaryComponentAllowedPrimitive that = (CoreDataTypeSupplementaryComponentAllowedPrimitive) o;

        if (cdtScAwdPriId != that.cdtScAwdPriId) return false;
        if (cdtScId != that.cdtScId) return false;
        if (cdtPriId != that.cdtPriId) return false;
        return isDefault == that.isDefault;

    }

    @Override
    public int hashCode() {
        int result = (int) (cdtScAwdPriId ^ (cdtScAwdPriId >>> 32));
        result = 31 * result + (int) (cdtScId ^ (cdtScId >>> 32));
        result = 31 * result + (int) (cdtPriId ^ (cdtPriId >>> 32));
        result = 31 * result + (isDefault ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CoreDataTypeSupplementaryComponentAllowedPrimitive{" +
                "cdtScAwdPriId=" + cdtScAwdPriId +
                ", cdtScId=" + cdtScId +
                ", cdtPriId=" + cdtPriId +
                ", isDefault=" + isDefault +
                '}';
    }
}
