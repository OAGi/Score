package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Auditable;

import java.math.BigInteger;

public class Asbiep extends Auditable {

    private BigInteger asbiepId;

    private String guid;

    private BigInteger basedAsccpManifestId;

    private String path;

    private String hashPath;

    private BigInteger roleOfAbieId;

    private String definition;

    private String remark;

    private String bizTerm;

    private String displayName;

    private BigInteger ownerTopLevelAsbiepId;

    public BigInteger getAsbiepId() {
        return asbiepId;
    }

    public void setAsbiepId(BigInteger asbiepId) {
        this.asbiepId = asbiepId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public BigInteger getBasedAsccpManifestId() {
        return basedAsccpManifestId;
    }

    public void setBasedAsccpManifestId(BigInteger basedAsccpManifestId) {
        this.basedAsccpManifestId = basedAsccpManifestId;
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

    public BigInteger getRoleOfAbieId() {
        return roleOfAbieId;
    }

    public void setRoleOfAbieId(BigInteger roleOfAbieId) {
        this.roleOfAbieId = roleOfAbieId;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public BigInteger getOwnerTopLevelAsbiepId() {
        return ownerTopLevelAsbiepId;
    }

    public void setOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
        this.ownerTopLevelAsbiepId = ownerTopLevelAsbiepId;
    }
}
