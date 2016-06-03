package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "asccp")
public class AssociationCoreComponentProperty implements Serializable {

    @Id
    @GeneratedValue(generator = "ASCCP_ID_SEQ", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "ASCCP_ID_SEQ", sequenceName = "ASCCP_ID_SEQ", allocationSize = 1)
    private int asccpId;

    @Column(nullable = false)
    private String guid;

    @Column(nullable = false)
    private String propertyTerm;

    @Lob
    @Column(nullable = false)
    private String definition;

    @Column(nullable = false)
    private int roleOfAccId;

    @Column(nullable = false)
    private String den;

    @Column(nullable = false)
    private int createdBy;

    @Column(nullable = false)
    private int ownerUserId;

    @Column(nullable = false)
    private int lastUpdatedBy;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

    @Column(nullable = false)
    private int state;

    @Column(nullable = false)
    private String module;

    @Column(nullable = false)
    private int namespaceId;

    @Column(nullable = false)
    private boolean reusableIndicator;

    @Column(name = "is_deprecated", nullable = false)
    private boolean deprecated;

    @Column(nullable = false)
    private int revisionNum;

    @Column(nullable = false)
    private int revisionTrackingNum;

    @Column
    private Integer revisionAction;

    @Column
    private Integer releaseId;

    @Column
    private Integer currentAsccpId;

    public AssociationCoreComponentProperty() {}

    public AssociationCoreComponentProperty(int asccpId, String den) {
        this.asccpId = asccpId;
        this.den = den;
    }

    public AssociationCoreComponentProperty(int asccpId, int roleOfAccId, String definition) {
        this.asccpId = asccpId;
        this.roleOfAccId = roleOfAccId;
        this.definition = definition;
    }

    @PrePersist
    public void prePersist() {
        creationTimestamp = new Date();
        lastUpdateTimestamp = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdateTimestamp = new Date();
    }

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
        return (revisionAction == null) ? 0 : revisionAction;
    }

    public void setRevisionAction(int revisionAction) {
        this.revisionAction = revisionAction;
    }

    public int getReleaseId() {
        return (releaseId == null) ? 0 : releaseId;
    }

    public void setReleaseId(int releaseId) {
        this.releaseId = releaseId;
    }

    public int getCurrentAsccpId() {
        return (currentAsccpId == null) ? 0 : currentAsccpId;
    }

    public void setCurrentAsccpId(int currentAsccpId) {
        this.currentAsccpId = currentAsccpId;
    }

    @Override
    public String toString() {
        return "AssociationCoreComponentProperty{" +
                "asccpId=" + asccpId +
                ", guid='" + guid + '\'' +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", definition='" + definition + '\'' +
                ", roleOfAccId=" + roleOfAccId +
                ", den='" + den + '\'' +
                ", createdBy=" + createdBy +
                ", ownerUserId=" + ownerUserId +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", state=" + state +
                ", module='" + module + '\'' +
                ", namespaceId=" + namespaceId +
                ", reusableIndicator=" + reusableIndicator +
                ", deprecated=" + deprecated +
                ", revisionNum=" + revisionNum +
                ", revisionTrackingNum=" + revisionTrackingNum +
                ", revisionAction=" + revisionAction +
                ", releaseId=" + releaseId +
                ", currentAsccpId=" + currentAsccpId +
                '}';
    }
}
