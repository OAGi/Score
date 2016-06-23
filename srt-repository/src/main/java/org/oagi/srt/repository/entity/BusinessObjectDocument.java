package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "bod")
public class BusinessObjectDocument implements Serializable {

    public static final String SEQUENCE_NAME = "BOD_ID_SEQ";

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
    private int bodId;

    @Column(nullable = false)
    private int bizCtxId;

    @Column
    private Integer topLevelAbieId;

    @Column
    private int state;

    public int getBodId() {
        return bodId;
    }

    public void setBodId(int bodId) {
        this.bodId = bodId;
    }

    public int getBizCtxId() {
        return bizCtxId;
    }

    public void setBizCtxId(int bizCtxId) {
        this.bizCtxId = bizCtxId;
    }

    public int getTopLevelAbieId() {
        return (topLevelAbieId == null) ? 0 : topLevelAbieId;
    }

    public void setTopLevelAbieId(int topLevelAbieId) {
        this.topLevelAbieId = topLevelAbieId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BusinessObjectDocument that = (BusinessObjectDocument) o;

        if (bodId != that.bodId) return false;
        if (bizCtxId != that.bizCtxId) return false;
        if (state != that.state) return false;
        return topLevelAbieId != null ? topLevelAbieId.equals(that.topLevelAbieId) : that.topLevelAbieId == null;

    }

    @Override
    public int hashCode() {
        int result = bodId;
        result = 31 * result + bizCtxId;
        result = 31 * result + (topLevelAbieId != null ? topLevelAbieId.hashCode() : 0);
        result = 31 * result + state;
        return result;
    }

    @Override
    public String toString() {
        return "BusinessObjectDocument{" +
                "bodId=" + bodId +
                ", bizCtxId=" + bizCtxId +
                ", topLevelAbieId=" + topLevelAbieId +
                ", state=" + state +
                '}';
    }
}
