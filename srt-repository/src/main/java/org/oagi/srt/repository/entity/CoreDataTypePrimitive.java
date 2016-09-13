package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cdt_pri")
public class CoreDataTypePrimitive implements Serializable {

    public static final String SEQUENCE_NAME = "CDT_PRI_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long cdtPriId;

    @Column(nullable = false, length = 45)
    private String name;

    public long getCdtPriId() {
        return cdtPriId;
    }

    public void setCdtPriId(long cdtPriId) {
        this.cdtPriId = cdtPriId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoreDataTypePrimitive that = (CoreDataTypePrimitive) o;

        if (cdtPriId != that.cdtPriId) return false;
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (cdtPriId ^ (cdtPriId >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CoreDataTypePrimitive{" +
                "cdtPriId=" + cdtPriId +
                ", name='" + name + '\'' +
                '}';
    }
}
