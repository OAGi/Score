package org.oagi.srt.repository.entity;

import java.io.Serializable;

public class CoreDataTypeSupplementaryComponentAllowedPrimitive implements Serializable {

    private int cdtScAwdPriId;
    private int cdtScId;
    private int cdtPriId;
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
