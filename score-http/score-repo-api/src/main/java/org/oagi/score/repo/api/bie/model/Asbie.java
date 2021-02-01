package org.oagi.score.repo.api.bie.model;

import java.math.BigInteger;

public class Asbie implements BieAssociation {

    private BigInteger asbieId;

    private String guid;

    private BigInteger basedAsccManifestId;

    private String path;

    private String hashPath;

    private BigInteger fromAbieId;

    private BigInteger toAsbiepId;

    private int cardinalityMin;

    private int cardinalityMax;

    private boolean nillable;

    private String definition;

    private String remark;

    private boolean used;

    private BigInteger ownerTopLevelAsbiepId;

    public BigInteger getAsbieId() {
        return asbieId;
    }

    public void setAsbieId(BigInteger asbieId) {
        this.asbieId = asbieId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public BigInteger getBasedAsccManifestId() {
        return basedAsccManifestId;
    }

    public void setBasedAsccManifestId(BigInteger basedAsccManifestId) {
        this.basedAsccManifestId = basedAsccManifestId;
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

    public BigInteger getToAsbiepId() {
        return toAsbiepId;
    }

    public void setToAsbiepId(BigInteger toAsbiepId) {
        this.toAsbiepId = toAsbiepId;
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
        return true;
    }

    @Override
    public boolean isBbie() {
        return false;
    }
}
