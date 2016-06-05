package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cdt_awd_pri_xps_type_map")
public class CoreDataTypeAllowedPrimitiveExpressionTypeMap implements Serializable {

    public static final String SEQUENCE_NAME = "CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.AUTO)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private int cdtAwdPriXpsTypeMapId;

    @Column(nullable = false)
    private int cdtAwdPriId;

    @Column(nullable = false)
    private int xbtId;

    public int getCdtAwdPriXpsTypeMapId() {
        return cdtAwdPriXpsTypeMapId;
    }

    public void setCdtAwdPriXpsTypeMapId(int cdtAwdPriXpsTypeMapId) {
        this.cdtAwdPriXpsTypeMapId = cdtAwdPriXpsTypeMapId;
    }

    public int getCdtAwdPriId() {
        return cdtAwdPriId;
    }

    public void setCdtAwdPriId(int cdtAwdPriId) {
        this.cdtAwdPriId = cdtAwdPriId;
    }

    public int getXbtId() {
        return xbtId;
    }

    public void setXbtId(int xbtId) {
        this.xbtId = xbtId;
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
