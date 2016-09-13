package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "dt_sc")
public class DataTypeSupplementaryComponent implements Serializable {

    public static final String SEQUENCE_NAME = "DT_SC_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long dtScId;

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
    private long ownerDtId;

    @Column(nullable = false)
    private int cardinalityMin;

    @Column
    private int cardinalityMax;

    @Column
    private Long basedDtScId;

    public long getDtScId() {
        return dtScId;
    }

    public void setDtScId(long dtScId) {
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

    public long getOwnerDtId() {
        return ownerDtId;
    }

    public void setOwnerDtId(long ownerDtId) {
        this.ownerDtId = ownerDtId;
    }

    public int getCardinalityMin() {
        return cardinalityMin;
    }

    public void setCardinalityMin(int cardinalityMin) {
        this.cardinalityMin = cardinalityMin;
    }

    public int getCardinalityMax() {
        return cardinalityMax;
    }

    public void setCardinalityMax(int cardinalityMax) {
        this.cardinalityMax = cardinalityMax;
    }

    public long getBasedDtScId() {
        return (basedDtScId == null) ? 0L : basedDtScId;
    }

    public void setBasedDtScId(long basedDtScId) {
        this.basedDtScId = basedDtScId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataTypeSupplementaryComponent that = (DataTypeSupplementaryComponent) o;

        if (dtScId != that.dtScId) return false;
        if (ownerDtId != that.ownerDtId) return false;
        if (cardinalityMin != that.cardinalityMin) return false;
        if (cardinalityMax != that.cardinalityMax) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (propertyTerm != null ? !propertyTerm.equals(that.propertyTerm) : that.propertyTerm != null) return false;
        if (representationTerm != null ? !representationTerm.equals(that.representationTerm) : that.representationTerm != null)
            return false;
        if (definition != null ? !definition.equals(that.definition) : that.definition != null) return false;
        return basedDtScId != null ? basedDtScId.equals(that.basedDtScId) : that.basedDtScId == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (dtScId ^ (dtScId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (representationTerm != null ? representationTerm.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (int) (ownerDtId ^ (ownerDtId >>> 32));
        result = 31 * result + cardinalityMin;
        result = 31 * result + cardinalityMax;
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
                ", cardinalityMin=" + cardinalityMin +
                ", cardinalityMax=" + cardinalityMax +
                ", basedDtScId=" + basedDtScId +
                '}';
    }
}
