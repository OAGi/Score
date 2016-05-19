package org.oagi.srt.repository.entity;

import java.io.Serializable;

public class DataTypeSupplementaryComponent implements Serializable {

    private int dtScId;
    private String guid;
    private String propertyTerm;
    private String representationTerm;
    private String definition;
    private int ownerDtId;
    private int minCardinality;
    private int maxCardinality;
    private int basedDtScId;

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
        return basedDtScId;
    }

    public void setBasedDtScId(int basedDtScId) {
        this.basedDtScId = basedDtScId;
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
