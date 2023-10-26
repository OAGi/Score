package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Auditable;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BbieSc extends Auditable {

    private BigInteger bbieScId;

    private String guid;

    private BigInteger basedDtScManifestId;

    private String path;

    private String hashPath;

    private BigInteger bbieId;

    private BigInteger dtScPriRestriId;

    private BigInteger codeListManifestId;

    private BigInteger agencyIdListManifestId;

    private String defaultValue;

    private String fixedValue;
    private BigInteger facetMinLength;
    private BigInteger facetMaxLength;
    private String facetPattern;
    private String facetMinInclusive;
    private String facetMinExclusive;
    private String facetMaxInclusive;
    private String facetMaxExclusive;

    private int cardinalityMin;

    private int cardinalityMax;

    private boolean nillable;

    private String definition;

    private String remark;

    private String bizTerm;

    private String example;

    private boolean deprecated;

    private boolean used;

    private BigInteger ownerTopLevelAsbiepId;

    public BigInteger getBbieScId() {
        return bbieScId;
    }

    public void setBbieScId(BigInteger bbieScId) {
        this.bbieScId = bbieScId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public BigInteger getBasedDtScManifestId() {
        return basedDtScManifestId;
    }

    public void setBasedDtScManifestId(BigInteger basedDtScManifestId) {
        this.basedDtScManifestId = basedDtScManifestId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHashPath() {
        return hashPath;
    }

    public void setHashPath(String hashPath) {
        this.hashPath = hashPath;
    }

    public BigInteger getBbieId() {
        return bbieId;
    }

    public void setBbieId(BigInteger bbieId) {
        this.bbieId = bbieId;
    }

    public BigInteger getDtScPriRestriId() {
        return dtScPriRestriId;
    }

    public void setDtScPriRestriId(BigInteger dtScPriRestriId) {
        this.dtScPriRestriId = dtScPriRestriId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }

    public void setCodeListManifestId(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getAgencyIdListManifestId() {
        return agencyIdListManifestId;
    }

    public void setAgencyIdListManifestId(BigInteger agencyIdListManifestId) {
        this.agencyIdListManifestId = agencyIdListManifestId;
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

    public BigInteger getFacetMinLength() {
        return facetMinLength;
    }

    public void setFacetMinLength(BigInteger facetMinLength) {
        this.facetMinLength = facetMinLength;
    }

    public BigInteger getFacetMaxLength() {
        return facetMaxLength;
    }

    public void setFacetMaxLength(BigInteger facetMaxLength) {
        this.facetMaxLength = facetMaxLength;
    }

    public String getFacetPattern() {
        return facetPattern;
    }

    public void setFacetPattern(String facetPattern) {
        this.facetPattern = facetPattern;
    }

    public String getFacetMinInclusive() {
        return facetMinInclusive;
    }

    public void setFacetMinInclusive(String facetMinInclusive) {
        this.facetMinInclusive = facetMinInclusive;
    }

    public String getFacetMinExclusive() {
        return facetMinExclusive;
    }

    public void setFacetMinExclusive(String facetMinExclusive) {
        this.facetMinExclusive = facetMinExclusive;
    }

    public String getFacetMaxInclusive() {
        return facetMaxInclusive;
    }

    public void setFacetMaxInclusive(String facetMaxInclusive) {
        this.facetMaxInclusive = facetMaxInclusive;
    }

    public String getFacetMaxExclusive() {
        return facetMaxExclusive;
    }

    public void setFacetMaxExclusive(String facetMaxExclusive) {
        this.facetMaxExclusive = facetMaxExclusive;
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

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public BigInteger getOwnerTopLevelAsbiepId() {
        return ownerTopLevelAsbiepId;
    }

    public void setOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
        this.ownerTopLevelAsbiepId = ownerTopLevelAsbiepId;
    }
}
