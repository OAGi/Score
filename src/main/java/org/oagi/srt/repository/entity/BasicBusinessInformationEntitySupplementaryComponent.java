package org.oagi.srt.repository.entity;

import java.io.Serializable;

public class BasicBusinessInformationEntitySupplementaryComponent implements Serializable {

    private int bbieScId;
    private int bbieId;
    private int dtScId;
    private int dtScPriRestriId;
    private int codeListId;
    private int agencyIdListId;
    private int minCardinality;
    private int maxCardinality;
    private String defaultValue;
    private String fixedValue;
    private String definition;
    private String remark;
    private String bizTerm;
    private boolean used;

    public int getBbieScId() {
        return bbieScId;
    }

    public void setBbieScId(int bbieScId) {
        this.bbieScId = bbieScId;
    }

    public int getBbieId() {
        return bbieId;
    }

    public void setBbieId(int bbieId) {
        this.bbieId = bbieId;
    }

    public int getDtScId() {
        return dtScId;
    }

    public void setDtScId(int dtScId) {
        this.dtScId = dtScId;
    }

    public int getDtScPriRestriId() {
        return dtScPriRestriId;
    }

    public void setDtScPriRestriId(int dtScPriRestriId) {
        this.dtScPriRestriId = dtScPriRestriId;
    }

    public int getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(int codeListId) {
        this.codeListId = codeListId;
    }

    public int getAgencyIdListId() {
        return agencyIdListId;
    }

    public void setAgencyIdListId(int agencyIdListId) {
        this.agencyIdListId = agencyIdListId;
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

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getBizTerm() {
        return bizTerm;
    }

    public void setBizTerm(String bizTerm) {
        this.bizTerm = bizTerm;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    @Override
    public String toString() {
        return "BasicBusinessInformationEntitySupplementaryComponent{" +
                "bbieScId=" + bbieScId +
                ", bbieId=" + bbieId +
                ", dtScId=" + dtScId +
                ", dtScPriRestriId=" + dtScPriRestriId +
                ", codeListId=" + codeListId +
                ", agencyIdListId=" + agencyIdListId +
                ", minCardinality=" + minCardinality +
                ", maxCardinality=" + maxCardinality +
                ", defaultValue='" + defaultValue + '\'' +
                ", fixedValue='" + fixedValue + '\'' +
                ", definition='" + definition + '\'' +
                ", remark='" + remark + '\'' +
                ", bizTerm='" + bizTerm + '\'' +
                ", used=" + used +
                '}';
    }
}
