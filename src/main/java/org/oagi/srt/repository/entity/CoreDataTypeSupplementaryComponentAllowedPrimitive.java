package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cdt_sc_awd_pri")
public class CoreDataTypeSupplementaryComponentAllowedPrimitive implements Serializable {

    public static final String SEQUENCE_NAME = "CDT_SC_AWD_PRI_ID_SEQ";

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
    private int cdtScAwdPriId;

    @Column(nullable = false)
    private int cdtScId;

    @Column(nullable = false)
    private int cdtPriId;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    public int getCdtScAwdPriId() {
        return cdtScAwdPriId;
    }

    public void setCdtScAwdPriId(int cdtScAwdPriId) {
        this.cdtScAwdPriId = cdtScAwdPriId;
    }

    public int getCdtScId() {
        return cdtScId;
    }

    public void setCdtScId(int cdtScId) {
        this.cdtScId = cdtScId;
    }

    public int getCdtPriId() {
        return cdtPriId;
    }

    public void setCdtPriId(int cdtPriId) {
        this.cdtPriId = cdtPriId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
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
