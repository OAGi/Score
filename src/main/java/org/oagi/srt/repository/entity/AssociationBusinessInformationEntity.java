package org.oagi.srt.repository.entity;

import java.util.Date;

public class AssociationBusinessInformationEntity implements BusinessInformationEntity {
    private int asbieId;
    private String guid;
    private int fromAbieId;
    private int toAsbiepId;
    private int basedAscc;
    private String definition;
    private int cardinalityMin;
    private int cardinalityMax;
    private boolean nillable;
    private String remark;
    private int createdBy;
    private int lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private int seqKey;
    private boolean used;

    public int getAsbieId() {
        return asbieId;
    }

    public void setAsbieId(int asbieId) {
        this.asbieId = asbieId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getFromAbieId() {
        return fromAbieId;
    }

    public void setFromAbieId(int fromAbieId) {
        this.fromAbieId = fromAbieId;
    }

    public int getToAsbiepId() {
        return toAsbiepId;
    }

    public void setToAsbiepId(int toAsbiepId) {
        this.toAsbiepId = toAsbiepId;
    }

    public int getBasedAscc() {
        return basedAscc;
    }

    public void setBasedAscc(int basedAscc) {
        this.basedAscc = basedAscc;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
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

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(int lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public int getSeqKey() {
        return seqKey;
    }

    public void setSeqKey(int seqKey) {
        this.seqKey = seqKey;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
