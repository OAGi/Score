package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cdt_pri")
public class CoreDataTypePrimitive implements Serializable {

    public static final String SEQUENCE_NAME = "CDT_PRI_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.AUTO)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private int cdtPriId;

    @Column(nullable = false)
    private String name;

    public int getCdtPriId() {
        return cdtPriId;
    }

    public void setCdtPriId(int cdtPriId) {
        this.cdtPriId = cdtPriId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CoreDataTypePrimitive{" +
                "cdtPriId=" + cdtPriId +
                ", name='" + name + '\'' +
                '}';
    }
}
