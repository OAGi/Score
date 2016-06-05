package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cdt_awd_pri")
public class CoreDataTypeAllowedPrimitive implements Serializable {

    public static final String SEQUENCE_NAME = "CDT_AWD_PRI_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.AUTO)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private int cdtAwdPriId;

    @Column(nullable = false)
    private int cdtId;

    @Column(nullable = false)
    private int cdtPriId;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    public int getCdtAwdPriId() {
        return cdtAwdPriId;
    }

    public void setCdtAwdPriId(int cdtAwdPriId) {
        this.cdtAwdPriId = cdtAwdPriId;
    }

    public int getCdtId() {
        return cdtId;
    }

    public void setCdtId(int cdtId) {
        this.cdtId = cdtId;
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
        return "CoreDataTypeAllowedPrimitive{" +
                "cdtAwdPriId=" + cdtAwdPriId +
                ", cdtId=" + cdtId +
                ", cdtPriId=" + cdtPriId +
                ", isDefault=" + isDefault +
                '}';
    }
}
