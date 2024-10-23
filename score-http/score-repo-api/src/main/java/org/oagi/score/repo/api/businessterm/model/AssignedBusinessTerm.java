package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Auditable;

import java.math.BigInteger;
import java.util.Date;

public class AssignedBusinessTerm extends Auditable {

    private BigInteger assignedBizTermId;
    private BigInteger bieId;
    private String bieType;
    private boolean primaryIndicator;
    private String typeCode;
    private String den;
    private BigInteger businessTermId;
    private String businessTerm;
    private String externalReferenceUri;
    private Date lastUpdateTimestamp;

    private String owner;
    private String lastUpdateUser;

    public AssignedBusinessTerm() {
    }

    public AssignedBusinessTerm(BigInteger assignedBizTermId, BigInteger bieId, String bieType,
                                boolean primaryIndicator, String typeCode, String den,
                                BigInteger businessTermId, String businessTerm,
                                String externalReferenceUri, Date lastUpdateTimestamp,
                                String owner, String lastUpdateUser) {
        this.assignedBizTermId = assignedBizTermId;
        this.bieId = bieId;
        this.bieType = bieType;
        this.primaryIndicator = primaryIndicator;
        this.typeCode = typeCode;
        this.den = den;
        this.businessTermId = businessTermId;
        this.businessTerm = businessTerm;
        this.externalReferenceUri = externalReferenceUri;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.owner = owner;
        this.lastUpdateUser = lastUpdateUser;
    }

    public BigInteger getAssignedBizTermId() {
        return assignedBizTermId;
    }

    public void setAssignedBizTermId(BigInteger assignedBizTermId) {
        this.assignedBizTermId = assignedBizTermId;
    }

    public BigInteger getBieId() {
        return bieId;
    }

    public void setBieId(BigInteger bieId) {
        this.bieId = bieId;
    }

    public boolean isPrimaryIndicator() {
        return primaryIndicator;
    }

    public void setPrimaryIndicator(boolean primaryIndicator) {
        this.primaryIndicator = primaryIndicator;
    }

    public BigInteger getBusinessTermId() {
        return businessTermId;
    }

    public void setBusinessTermId(BigInteger businessTermId) {
        this.businessTermId = businessTermId;
    }

    public String getBieType() {
        return bieType;
    }

    public void setBieType(String bieType) {
        this.bieType = bieType;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public String getBusinessTerm() {
        return businessTerm;
    }

    public void setBusinessTerm(String businessTerm) {
        this.businessTerm = businessTerm;
    }

    public String getExternalReferenceUri() {
        return externalReferenceUri;
    }

    public void setExternalReferenceUri(String externalReferenceUri) {
        this.externalReferenceUri = externalReferenceUri;
    }

    @Override
    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLastUpdateUser() {
        return lastUpdateUser;
    }

    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }

}
