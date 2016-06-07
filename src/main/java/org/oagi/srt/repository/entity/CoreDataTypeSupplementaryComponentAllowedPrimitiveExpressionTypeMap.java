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
    private int cdtScAwdPriXpsTypeMapId;

    @Column(nullable = false)
    private int cdtScAwdPri;

    @Column(nullable = false)
    private int xbtId;

    public int getCdtScAwdPriXpsTypeMapId() {
        return cdtScAwdPriXpsTypeMapId;
    }

    public void setCdtScAwdPriXpsTypeMapId(int cdtScAwdPriXpsTypeMapId) {
        this.cdtScAwdPriXpsTypeMapId = cdtScAwdPriXpsTypeMapId;
    }

    public int getCdtScAwdPri() {
        return cdtScAwdPri;
    }

    public void setCdtScAwdPri(int cdtScAwdPri) {
        this.cdtScAwdPri = cdtScAwdPri;
    }

    public int getXbtId() {
        return xbtId;
    }

    public void setXbtId(int xbtId) {
        this.xbtId = xbtId;
    }

    @Override
    public String toString() {
        return "CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap{" +
                "cdtScAwdPriXpsTypeMapId=" + cdtScAwdPriXpsTypeMapId +
                ", cdtScAwdPri=" + cdtScAwdPri +
                ", xbtId=" + xbtId +
                '}';
    }
}
