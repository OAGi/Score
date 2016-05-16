package org.oagi.srt.repository.entity;

import java.util.Date;

public class AssociationCoreComponentProperty {

    private int asccpId;
    private String guid;
    private String propertyTerm;
    private String definition;
    private int roleOfAccId;
    private String den;
    private int createdBy;
    private int ownerUserId;
    private int lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private int state;
    private String module;
    private int namespaceId;
    private boolean reusableIndicator;
    private boolean deprecated;
    private int revisionNum;
    private int revisionTrackingNum;
    private int revisionAction;
    private int releaseId;
    private int currentAsccpId;

    public int getAsccpId() {
        return asccpId;
    }

    public void setAsccpId(int asccpId) {
        this.asccpId = asccpId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getRoleOfAccId() {
        return roleOfAccId;
    }

    public void setRoleOfAccId(int roleOfAccId) {
        this.roleOfAccId = roleOfAccId;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(int ownerUserId) {
        this.ownerUserId = ownerUserId;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public int getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(int namespaceId) {
        this.namespaceId = namespaceId;
    }

    public boolean isReusableIndicator() {
        return reusableIndicator;
    }

    public void setReusableIndicator(boolean reusableIndicator) {
        this.reusableIndicator = reusableIndicator;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public int getRevisionNum() {
        return revisionNum;
    }

    public void setRevisionNum(int revisionNum) {
        this.revisionNum = revisionNum;
    }

    public int getRevisionTrackingNum() {
        return revisionTrackingNum;
    }

    public void setRevisionTrackingNum(int revisionTrackingNum) {
        this.revisionTrackingNum = revisionTrackingNum;
    }

    public int getRevisionAction() {
        return revisionAction;
    }

    public void setRevisionAction(int revisionAction) {
        this.revisionAction = revisionAction;
    }

    public int getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(int releaseId) {
        this.releaseId = releaseId;
    }

    public int getCurrentAsccpId() {
        return currentAsccpId;
    }

    public void setCurrentAsccpId(int currentAsccpId) {
        this.currentAsccpId = currentAsccpId;
    }
}
