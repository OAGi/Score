package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "bbie_sc")
public class BasicBusinessInformationEntitySupplementaryComponent implements Serializable, IdEntity {

    public static final String SEQUENCE_NAME = "BBIE_SC_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "2000"),
            }
    )
    private int bbieScId;

    @Column(nullable = false)
    private int bbieId;

    @Column(nullable = false)
    private int dtScId;

    @Column
    private Integer dtScPriRestriId;

    @Column
    private Integer codeListId;

    @Column
    private Integer agencyIdListId;

    @Column(nullable = false)
    private int minCardinality;

    @Column
    private int maxCardinality;

    @Column
    private String defaultValue;

    @Column
    private String fixedValue;

    @Lob
    @Column
    private String definition;

    @Column
    private String remark;

    @Column
    private String bizTerm;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @Override
    public int getId() {
        return getBbieScId();
    }

    @Override
    public void setId(int id) {
        setBbieScId(id);
    }

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
        return (dtScPriRestriId == null) ? 0 : dtScPriRestriId;
    }

    public void setDtScPriRestriId(int dtScPriRestriId) {
        this.dtScPriRestriId = dtScPriRestriId;
    }

    public int getCodeListId() {
        return (codeListId == null) ? 0 : codeListId;
    }

    public void setCodeListId(int codeListId) {
        this.codeListId = codeListId;
    }

    public int getAgencyIdListId() {
        return (agencyIdListId == null) ? 0 : agencyIdListId;
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
