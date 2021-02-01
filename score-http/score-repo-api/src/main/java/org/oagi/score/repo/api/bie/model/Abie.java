package org.oagi.score.repo.api.bie.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public class Abie implements Serializable {

    private BigInteger abieId;

    private String guid;

    private BigInteger basedAccManifestId;

    private String path;

    private String hashPath;

    private String definition;

    private String remark;

    private String bizTerm;

    private BigInteger ownerTopLevelAsbiepId;

    public BigInteger getAbieId() {
        return abieId;
    }

    public void setAbieId(BigInteger abieId) {
        this.abieId = abieId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public BigInteger getBasedAccManifestId() {
        return basedAccManifestId;
    }

    public void setBasedAccManifestId(BigInteger basedAccManifestId) {
        this.basedAccManifestId = basedAccManifestId;
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

    public BigInteger getOwnerTopLevelAsbiepId() {
        return ownerTopLevelAsbiepId;
    }

    public void setOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
        this.ownerTopLevelAsbiepId = ownerTopLevelAsbiepId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Abie abie = (Abie) o;
        if (abieId != null) {
            return Objects.equals(abieId, abie.abieId);
        } else {
            return Objects.equals(guid, abie.guid);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(abieId, guid);
    }
}
