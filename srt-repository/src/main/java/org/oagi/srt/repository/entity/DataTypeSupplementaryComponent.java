package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "dt_sc")
public class DataTypeSupplementaryComponent implements Serializable {

    public static final String SEQUENCE_NAME = "DT_SC_ID_SEQ";

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
    private int dtScId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(length = 60)
    private String propertyTerm;

    @Column(length = 20)
    private String representationTerm;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(nullable = false)
    private int ownerDtId;

    @Column(nullable = false)
    private int minCardinality;

    @Column
    private int maxCardinality;

    @Column
    private Integer basedDtScId;

    public int getDtScId() {
        return dtScId;
    }

    public void setDtScId(int dtScId) {
        this.dtScId = dtScId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public String getRepresentationTerm() {
        return representationTerm;
    }

    public void setRepresentationTerm(String representationTerm) {
        this.representationTerm = representationTerm;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getOwnerDtId() {
        return ownerDtId;
    }

    public void setOwnerDtId(int ownerDtId) {
        this.ownerDtId = ownerDtId;
    }

    public int getMinCardinality() {
        return minCardinality;
    }

    public void setMinCardinality(int minCardinality) {
        this.minCardinality = minCardinality;
    }

    public int getMaxCardinality() {
        return maxCardinality;
    }

    public void setMaxCardinality(int maxCardinality) {
        this.maxCardinality = maxCardinality;
    }

    public int getBasedDtScId() {
        return (basedDtScId == null) ? 0 : basedDtScId;
    }

    public void setBasedDtScId(int basedDtScId) {
        this.basedDtScId = basedDtScId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataTypeSupplementaryComponent that = (DataTypeSupplementaryComponent) o;

        if (dtScId != that.dtScId) return false;
        if (ownerDtId != that.ownerDtId) return false;
        if (minCardinality != that.minCardinality) return false;
        if (maxCardinality != that.maxCardinality) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (propertyTerm != null ? !propertyTerm.equals(that.propertyTerm) : that.propertyTerm != null) return false;
        if (representationTerm != null ? !representationTerm.equals(that.representationTerm) : that.representationTerm != null)
            return false;
        if (definition != null ? !definition.equals(that.definition) : that.definition != null) return false;
        return basedDtScId != null ? basedDtScId.equals(that.basedDtScId) : that.basedDtScId == null;

    }

    @Override
    public int hashCode() {
        int result = dtScId;
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (representationTerm != null ? representationTerm.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + ownerDtId;
        result = 31 * result + minCardinality;
        result = 31 * result + maxCardinality;
        result = 31 * result + (basedDtScId != null ? basedDtScId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DataTypeSupplementaryComponent{" +
                "dtScId=" + dtScId +
                ", guid='" + guid + '\'' +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", representationTerm='" + representationTerm + '\'' +
                ", definition='" + definition + '\'' +
                ", ownerDtId=" + ownerDtId +
                ", minCardinality=" + minCardinality +
                ", maxCardinality=" + maxCardinality +
                ", basedDtScId=" + basedDtScId +
                '}';
    }
}
