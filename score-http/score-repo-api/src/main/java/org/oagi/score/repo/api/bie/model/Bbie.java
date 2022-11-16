package org.oagi.score.repo.api.bie.model;

import java.math.BigInteger;

public class Bbie implements BieAssociation {

    private BigInteger bbieId;

    private String guid;

    private BigInteger basedBccManifestId;

    private String path;

    private String hashPath;

    private BigInteger fromAbieId;

    private BigInteger toBbiepId;

    private BigInteger bdtPriRestriId;

    private BigInteger codeListManifestId;

    private BigInteger agencyIdListManifestId;

    private String defaultValue;

    private String fixedValue;

    private int cardinalityMin;

    private int cardinalityMax;

    private boolean nillable;

    private String definition;

    private String remark;

    private String example;

    private boolean used;

    private BigInteger ownerTopLevelAsbiepId;

    public BigInteger getBbieId() {
        return bbieId;
    }

    public void setBbieId(BigInteger bbieId) {
        this.bbieId = bbieId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public BigInteger getBasedBccManifestId() {
        return basedBccManifestId;
    }

    public void setBasedBccManifestId(BigInteger basedBccManifestId) {
        this.basedBccManifestId = basedBccManifestId;
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

    public BigInteger getFromAbieId() {
        return fromAbieId;
    }

    public void setFromAbieId(BigInteger fromAbieId) {
        this.fromAbieId = fromAbieId;
    }

    public BigInteger getToBbiepId() {
        return toBbiepId;
    }

    public void setToBbiepId(BigInteger toBbiepId) {
        this.toBbiepId = toBbiepId;
    }

    public BigInteger getBdtPriRestriId() {
        return bdtPriRestriId;
    }

    public void setBdtPriRestriId(BigInteger bdtPriRestriId) {
        this.bdtPriRestriId = bdtPriRestriId;
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

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
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

    @Override
    public boolean isAsbie() {
        return false;
    }

    @Override
    public boolean isBbie() {
        return true;
    }
}
