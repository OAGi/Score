package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "bccp")
public class BasicCoreComponentProperty implements Serializable {

    public static final String SEQUENCE_NAME = "BCCP_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
            }
    )
    private int bccpId;

    @Column(nullable = false)
    private String guid;

    @Column(nullable = false)
    private String propertyTerm;

    @Column(nullable = false)
    private String representationTerm;

    @Column(nullable = false)
    private int bdtId;

    @Column(nullable = false)
    private String den;

    @Lob
    @Column
    private String definition;

    @Column
    private String module;

    @Column
    private Integer namespaceId;

    @Column(name = "is_deprecated", nullable = false)
    private boolean deprecated;

    @Column(nullable = false, updatable = false)
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
    private int revisionNum;

    @Column(nullable = false)
    private int revisionTrackingNum;

    @Column
    private int revisionAction = 1;

    @Column
    private Integer releaseId;

    @Column
    private Integer currentBccpId;

    public BasicCoreComponentProperty() {}

    public BasicCoreComponentProperty(int bccpId, String den) {
        this.bccpId = bccpId;
        this.den = den;
    }

    public BasicCoreComponentProperty(int bccpId, int bdtId, String definition) {
        this.bccpId = bccpId;
        this.bdtId = bdtId;
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

    public int getBccpId() {
        return bccpId;
    }

    public void setBccpId(int bccpId) {
        this.bccpId = bccpId;
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

    public String getRepresentationTerm() {
        return representationTerm;
    }

    public void setRepresentationTerm(String representationTerm) {
        this.representationTerm = representationTerm;
    }

    public int getBdtId() {
        return bdtId;
    }

    public void setBdtId(int bdtId) {
        this.bdtId = bdtId;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public int getNamespaceId() {
        return (namespaceId == null) ? 0 : namespaceId;
    }

    public void setNamespaceId(int namespaceId) {
        this.namespaceId = namespaceId;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
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
        return (releaseId == null) ? 0 : releaseId;
    }

    public void setReleaseId(int releaseId) {
        this.releaseId = releaseId;
    }

    public int getCurrentBccpId() {
        return (currentBccpId == null) ? 0 : currentBccpId;
    }

    public void setCurrentBccpId(int currentBccpId) {
        this.currentBccpId = currentBccpId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicCoreComponentProperty that = (BasicCoreComponentProperty) o;

        if (bccpId != that.bccpId) return false;
        if (bdtId != that.bdtId) return false;
        if (deprecated != that.deprecated) return false;
        if (createdBy != that.createdBy) return false;
        if (ownerUserId != that.ownerUserId) return false;
        if (lastUpdatedBy != that.lastUpdatedBy) return false;
        if (state != that.state) return false;
        if (revisionNum != that.revisionNum) return false;
        if (revisionTrackingNum != that.revisionTrackingNum) return false;
        if (revisionAction != that.revisionAction) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (propertyTerm != null ? !propertyTerm.equals(that.propertyTerm) : that.propertyTerm != null) return false;
        if (representationTerm != null ? !representationTerm.equals(that.representationTerm) : that.representationTerm != null)
            return false;
        if (den != null ? !den.equals(that.den) : that.den != null) return false;
        if (definition != null ? !definition.equals(that.definition) : that.definition != null) return false;
        if (module != null ? !module.equals(that.module) : that.module != null) return false;
        if (namespaceId != null ? !namespaceId.equals(that.namespaceId) : that.namespaceId != null) return false;
        if (creationTimestamp != null ? !creationTimestamp.equals(that.creationTimestamp) : that.creationTimestamp != null)
            return false;
        if (lastUpdateTimestamp != null ? !lastUpdateTimestamp.equals(that.lastUpdateTimestamp) : that.lastUpdateTimestamp != null)
            return false;
        if (releaseId != null ? !releaseId.equals(that.releaseId) : that.releaseId != null) return false;
        return currentBccpId != null ? currentBccpId.equals(that.currentBccpId) : that.currentBccpId == null;

    }

    @Override
    public int hashCode() {
        int result = bccpId;
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (representationTerm != null ? representationTerm.hashCode() : 0);
        result = 31 * result + bdtId;
        result = 31 * result + (den != null ? den.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + (namespaceId != null ? namespaceId.hashCode() : 0);
        result = 31 * result + (deprecated ? 1 : 0);
        result = 31 * result + createdBy;
        result = 31 * result + ownerUserId;
        result = 31 * result + lastUpdatedBy;
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + state;
        result = 31 * result + revisionNum;
        result = 31 * result + revisionTrackingNum;
        result = 31 * result + revisionAction;
        result = 31 * result + (releaseId != null ? releaseId.hashCode() : 0);
        result = 31 * result + (currentBccpId != null ? currentBccpId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BasicCoreComponentProperty{" +
                "bccpId=" + bccpId +
                ", guid='" + guid + '\'' +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", representationTerm='" + representationTerm + '\'' +
                ", bdtId=" + bdtId +
                ", den='" + den + '\'' +
                ", definition='" + definition + '\'' +
                ", module='" + module + '\'' +
                ", namespaceId=" + namespaceId +
                ", deprecated=" + deprecated +
                ", createdBy=" + createdBy +
                ", ownerUserId=" + ownerUserId +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", state=" + state +
                ", revisionNum=" + revisionNum +
                ", revisionTrackingNum=" + revisionTrackingNum +
                ", revisionAction=" + revisionAction +
                ", releaseId=" + releaseId +
                ", currentBccpId=" + currentBccpId +
                '}';
    }
}
