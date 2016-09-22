package org.oagi.srt.repository.entity;

import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "asccp")
public class AssociationCoreComponentProperty implements CoreComponentProperty, Serializable {

    public static final String SEQUENCE_NAME = "ASCCP_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long asccpId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false)
    private String propertyTerm;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(nullable = false)
    private long roleOfAccId;

    @Column(nullable = false, length = 200)
    private String den;

    @Column(nullable = false, updatable = false)
    private long createdBy;

    @Column(nullable = false)
    private long ownerUserId;

    @Column(nullable = false)
    private long lastUpdatedBy;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

    @Column(nullable = false)
    private int state;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id")
    private Module module;

    @Column(nullable = false)
    private long namespaceId;

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
    private Long releaseId;

    @Column
    private Long currentAsccpId;

    @Column(name = "is_nillable", nullable = false)
    private boolean nillable;

    public AssociationCoreComponentProperty() {}

    public AssociationCoreComponentProperty(long asccpId, String den) {
        this.asccpId = asccpId;
        this.den = den;
    }

    public AssociationCoreComponentProperty(long asccpId, long roleOfAccId, String definition) {
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

    public long getAsccpId() {
        return asccpId;
    }

    public void setAsccpId(long asccpId) {
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
        if (!StringUtils.isEmpty(definition)) {
            this.definition = definition;
        }
    }

    public long getRoleOfAccId() {
        return roleOfAccId;
    }

    public void setRoleOfAccId(long roleOfAccId) {
        this.roleOfAccId = roleOfAccId;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(long lastUpdatedBy) {
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

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public long getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(long namespaceId) {
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

    public long getReleaseId() {
        return (releaseId == null) ? 0L : releaseId;
    }

    public void setReleaseId(long releaseId) {
        this.releaseId = releaseId;
    }

    public long getCurrentAsccpId() {
        return (currentAsccpId == null) ? 0L : currentAsccpId;
    }

    public void setCurrentAsccpId(long currentAsccpId) {
        this.currentAsccpId = currentAsccpId;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssociationCoreComponentProperty that = (AssociationCoreComponentProperty) o;

        if (asccpId != that.asccpId) return false;
        if (roleOfAccId != that.roleOfAccId) return false;
        if (createdBy != that.createdBy) return false;
        if (ownerUserId != that.ownerUserId) return false;
        if (lastUpdatedBy != that.lastUpdatedBy) return false;
        if (state != that.state) return false;
        if (namespaceId != that.namespaceId) return false;
        if (reusableIndicator != that.reusableIndicator) return false;
        if (deprecated != that.deprecated) return false;
        if (revisionNum != that.revisionNum) return false;
        if (revisionTrackingNum != that.revisionTrackingNum) return false;
        if (nillable != that.nillable) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (propertyTerm != null ? !propertyTerm.equals(that.propertyTerm) : that.propertyTerm != null) return false;
        if (definition != null ? !definition.equals(that.definition) : that.definition != null) return false;
        if (den != null ? !den.equals(that.den) : that.den != null) return false;
        if (creationTimestamp != null ? !creationTimestamp.equals(that.creationTimestamp) : that.creationTimestamp != null)
            return false;
        if (lastUpdateTimestamp != null ? !lastUpdateTimestamp.equals(that.lastUpdateTimestamp) : that.lastUpdateTimestamp != null)
            return false;
        if (module != null ? !module.equals(that.module) : that.module != null) return false;
        if (revisionAction != null ? !revisionAction.equals(that.revisionAction) : that.revisionAction != null)
            return false;
        if (releaseId != null ? !releaseId.equals(that.releaseId) : that.releaseId != null) return false;
        return currentAsccpId != null ? currentAsccpId.equals(that.currentAsccpId) : that.currentAsccpId == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (asccpId ^ (asccpId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (int) (roleOfAccId ^ (roleOfAccId >>> 32));
        result = 31 * result + (den != null ? den.hashCode() : 0);
        result = 31 * result + (int) (createdBy ^ (createdBy >>> 32));
        result = 31 * result + (int) (ownerUserId ^ (ownerUserId >>> 32));
        result = 31 * result + (int) (lastUpdatedBy ^ (lastUpdatedBy >>> 32));
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + state;
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + (int) (namespaceId ^ (namespaceId >>> 32));
        result = 31 * result + (reusableIndicator ? 1 : 0);
        result = 31 * result + (deprecated ? 1 : 0);
        result = 31 * result + revisionNum;
        result = 31 * result + revisionTrackingNum;
        result = 31 * result + (revisionAction != null ? revisionAction.hashCode() : 0);
        result = 31 * result + (releaseId != null ? releaseId.hashCode() : 0);
        result = 31 * result + (currentAsccpId != null ? currentAsccpId.hashCode() : 0);
        result = 31 * result + (nillable ? 1 : 0);
        return result;
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
                ", module=" + module +
                ", namespaceId=" + namespaceId +
                ", reusableIndicator=" + reusableIndicator +
                ", deprecated=" + deprecated +
                ", revisionNum=" + revisionNum +
                ", revisionTrackingNum=" + revisionTrackingNum +
                ", revisionAction=" + revisionAction +
                ", releaseId=" + releaseId +
                ", currentAsccpId=" + currentAsccpId +
                ", nillable=" + nillable +
                '}';
    }
}
