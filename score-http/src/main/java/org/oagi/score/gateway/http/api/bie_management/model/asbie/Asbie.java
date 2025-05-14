package org.oagi.score.gateway.http.api.bie_management.model.asbie;

import org.oagi.score.gateway.http.api.bie_management.model.BieAssociation;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;

public class Asbie implements BieAssociation {

    private AsbieId asbieId;

    private String guid;

    private AsccManifestId basedAsccManifestId;

    private String path;

    private String hashPath;

    private AbieId fromAbieId;

    private AsbiepId toAsbiepId;

    private int cardinalityMin;

    private int cardinalityMax;

    private boolean nillable;

    private String definition;

    private String remark;

    private boolean deprecated;

    private boolean used;

    private TopLevelAsbiepId ownerTopLevelAsbiepId;

    public AsbieId getAsbieId() {
        return asbieId;
    }

    public void setAsbieId(AsbieId asbieId) {
        this.asbieId = asbieId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public AsccManifestId getBasedAsccManifestId() {
        return basedAsccManifestId;
    }

    public void setBasedAsccManifestId(AsccManifestId basedAsccManifestId) {
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

    public AbieId getFromAbieId() {
        return fromAbieId;
    }

    public void setFromAbieId(AbieId fromAbieId) {
        this.fromAbieId = fromAbieId;
    }

    public AsbiepId getToAsbiepId() {
        return toAsbiepId;
    }

    public void setToAsbiepId(AsbiepId toAsbiepId) {
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

    public TopLevelAsbiepId getOwnerTopLevelAsbiepId() {
        return ownerTopLevelAsbiepId;
    }

    public void setOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
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
