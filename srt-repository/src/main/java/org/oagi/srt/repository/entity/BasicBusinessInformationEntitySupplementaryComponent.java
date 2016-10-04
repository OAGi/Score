package org.oagi.srt.repository.entity;

import org.oagi.srt.common.util.Utility;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "bbie_sc")
public class BasicBusinessInformationEntitySupplementaryComponent implements Serializable, IdEntity {

    public static final String SEQUENCE_NAME = "BBIE_SC_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long bbieScId;

    @Column(nullable = false)
    private long bbieId;

    @Column(nullable = false)
    private long dtScId;

    @Column
    private Long dtScPriRestriId;

    @Column
    private Long codeListId;

    @Column
    private Long agencyIdListId;

    @Column(nullable = false)
    private int cardinalityMin;

    @Column
    private int cardinalityMax;

    @Column
    private String defaultValue;

    @Column
    private String fixedValue;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(length = 225)
    private String remark;

    @Column(length = 225)
    private String bizTerm;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @Column(nullable = false)
    private long ownerTopLevelAbieId;

    @Override
    public long getId() {
        return getBbieScId();
    }

    @Override
    public void setId(long id) {
        setBbieScId(id);
    }

    public long getBbieScId() {
        return bbieScId;
    }

    public void setBbieScId(long bbieScId) {
        this.bbieScId = bbieScId;
    }

    public String getGuid() {
        return Utility.generateGUID();
    }

    public long getBbieId() {
        return bbieId;
    }

    public void setBbieId(long bbieId) {
        this.bbieId = bbieId;
    }

    public long getDtScId() {
        return dtScId;
    }

    public void setDtScId(long dtScId) {
        this.dtScId = dtScId;
    }

    public long getDtScPriRestriId() {
        return (dtScPriRestriId == null) ? 0L : dtScPriRestriId;
    }

    public void setDtScPriRestriId(long dtScPriRestriId) {
        this.dtScPriRestriId = dtScPriRestriId;
    }

    public long getCodeListId() {
        return (codeListId == null) ? 0L : codeListId;
    }

    public void setCodeListId(long codeListId) {
        this.codeListId = codeListId;
    }

    public long getAgencyIdListId() {
        return (agencyIdListId == null) ? 0L : agencyIdListId;
    }

    public void setAgencyIdListId(long agencyIdListId) {
        this.agencyIdListId = agencyIdListId;
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
        if (!StringUtils.isEmpty(definition)) {
            this.definition = definition;
        }
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

    public long getOwnerTopLevelAbieId() {
        return ownerTopLevelAbieId;
    }

    public void setOwnerTopLevelAbieId(long ownerTopLevelAbieId) {
        this.ownerTopLevelAbieId = ownerTopLevelAbieId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicBusinessInformationEntitySupplementaryComponent that = (BasicBusinessInformationEntitySupplementaryComponent) o;

        if (bbieScId != that.bbieScId) return false;
        if (bbieId != that.bbieId) return false;
        if (dtScId != that.dtScId) return false;
        if (cardinalityMin != that.cardinalityMin) return false;
        if (cardinalityMax != that.cardinalityMax) return false;
        if (used != that.used) return false;
        if (ownerTopLevelAbieId != that.ownerTopLevelAbieId) return false;
        if (dtScPriRestriId != null ? !dtScPriRestriId.equals(that.dtScPriRestriId) : that.dtScPriRestriId != null)
            return false;
        if (codeListId != null ? !codeListId.equals(that.codeListId) : that.codeListId != null) return false;
        if (agencyIdListId != null ? !agencyIdListId.equals(that.agencyIdListId) : that.agencyIdListId != null)
            return false;
        if (defaultValue != null ? !defaultValue.equals(that.defaultValue) : that.defaultValue != null) return false;
        if (fixedValue != null ? !fixedValue.equals(that.fixedValue) : that.fixedValue != null) return false;
        if (definition != null ? !definition.equals(that.definition) : that.definition != null) return false;
        if (remark != null ? !remark.equals(that.remark) : that.remark != null) return false;
        return bizTerm != null ? bizTerm.equals(that.bizTerm) : that.bizTerm == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (bbieScId ^ (bbieScId >>> 32));
        result = 31 * result + (int) (bbieId ^ (bbieId >>> 32));
        result = 31 * result + (int) (dtScId ^ (dtScId >>> 32));
        result = 31 * result + (dtScPriRestriId != null ? dtScPriRestriId.hashCode() : 0);
        result = 31 * result + (codeListId != null ? codeListId.hashCode() : 0);
        result = 31 * result + (agencyIdListId != null ? agencyIdListId.hashCode() : 0);
        result = 31 * result + cardinalityMin;
        result = 31 * result + cardinalityMax;
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (fixedValue != null ? fixedValue.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (bizTerm != null ? bizTerm.hashCode() : 0);
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + (int) (ownerTopLevelAbieId ^ (ownerTopLevelAbieId >>> 32));
        return result;
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
                ", cardinalityMin=" + cardinalityMin +
                ", cardinalityMax=" + cardinalityMax +
                ", defaultValue='" + defaultValue + '\'' +
                ", fixedValue='" + fixedValue + '\'' +
                ", definition='" + definition + '\'' +
                ", remark='" + remark + '\'' +
                ", bizTerm='" + bizTerm + '\'' +
                ", used=" + used +
                ", ownerTopLevelAbieId=" + ownerTopLevelAbieId +
                '}';
    }
}
