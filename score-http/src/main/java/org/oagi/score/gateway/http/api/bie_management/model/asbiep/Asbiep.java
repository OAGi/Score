package org.oagi.score.gateway.http.api.bie_management.model.asbiep;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.common.model.base.Auditable;

public class Asbiep extends Auditable {

    private AsbiepId asbiepId;

    private String guid;

    private AsccpManifestId basedAsccpManifestId;

    private String path;

    private String hashPath;

    private AbieId roleOfAbieId;

    private String definition;

    private String remark;

    private String bizTerm;

    private String displayName;

    private TopLevelAsbiepId ownerTopLevelAsbiepId;

    public AsbiepId getAsbiepId() {
        return asbiepId;
    }

    public void setAsbiepId(AsbiepId asbiepId) {
        this.asbiepId = asbiepId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public AsccpManifestId getBasedAsccpManifestId() {
        return basedAsccpManifestId;
    }

    public void setBasedAsccpManifestId(AsccpManifestId basedAsccpManifestId) {
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

    public AbieId getRoleOfAbieId() {
        return roleOfAbieId;
    }

    public void setRoleOfAbieId(AbieId roleOfAbieId) {
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

    public TopLevelAsbiepId getOwnerTopLevelAsbiepId() {
        return ownerTopLevelAsbiepId;
    }

    public void setOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        this.ownerTopLevelAsbiepId = ownerTopLevelAsbiepId;
    }
}
