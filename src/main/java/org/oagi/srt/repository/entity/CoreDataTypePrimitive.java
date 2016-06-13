package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cdt_pri")
public class CoreDataTypePrimitive implements Serializable {

    @Id
    @GeneratedValue(generator = "CDT_PRI_ID_SEQ", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "CDT_PRI_ID_SEQ", sequenceName = "CDT_PRI_ID_SEQ", allocationSize = 1)
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
