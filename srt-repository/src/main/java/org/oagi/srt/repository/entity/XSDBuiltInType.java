package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "xbt")
public class XSDBuiltInType implements Serializable {

    public static final String SEQUENCE_NAME = "XBT_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long xbtId;

    @Column(length = 45)
    private String name;

    @Column(name = "builtin_type", length = 45)
    private String builtInType;

    @Column
    private Long subtypeOfXbtId;

    public long getXbtId() {
        return xbtId;
    }

    public void setXbtId(long xbtId) {
        this.xbtId = xbtId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuiltInType() {
        return builtInType;
    }

    public void setBuiltInType(String builtInType) {
        this.builtInType = builtInType;
    }

    public long getSubtypeOfXbtId() {
        return (subtypeOfXbtId == null) ? 0L : subtypeOfXbtId;
    }

    public void setSubtypeOfXbtId(long subtypeOfXbtId) {
        this.subtypeOfXbtId = subtypeOfXbtId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XSDBuiltInType that = (XSDBuiltInType) o;

        if (xbtId != that.xbtId) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (builtInType != null ? !builtInType.equals(that.builtInType) : that.builtInType != null) return false;
        return subtypeOfXbtId != null ? subtypeOfXbtId.equals(that.subtypeOfXbtId) : that.subtypeOfXbtId == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (xbtId ^ (xbtId >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (builtInType != null ? builtInType.hashCode() : 0);
        result = 31 * result + (subtypeOfXbtId != null ? subtypeOfXbtId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "XSDBuiltInType{" +
                "xbtId=" + xbtId +
                ", name='" + name + '\'' +
                ", builtInType='" + builtInType + '\'' +
                ", subtypeOfXbtId=" + subtypeOfXbtId +
                '}';
    }
}
