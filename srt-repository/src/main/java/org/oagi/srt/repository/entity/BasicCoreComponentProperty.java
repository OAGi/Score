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
    private long bccpId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false, length = 60)
    private String propertyTerm;

    @Column(nullable = false, length = 20)
    private String representationTerm;

    @Column(nullable = false)
    private long bdtId;

    @Column(nullable = false, length = 200)
    private String den;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id")
    private Module module;

    @Column
    private Long namespaceId;

    @Column(name = "is_deprecated", nullable = false)
    private boolean deprecated;

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

    @Column(nullable = false)
    private int revisionNum;

    @Column(nullable = false)
    private int revisionTrackingNum;

    @Column
    private int revisionAction = 1;

    @Column
    private Long releaseId;

    @Column
    private Long currentBccpId;

    @Column(name = "is_nillable", nullable = false)
    private boolean nillable;

    @Column
    private String defaultValue;

    public BasicCoreComponentProperty() {}

    public BasicCoreComponentProperty(long bccpId, String den) {
        this.bccpId = bccpId;
        this.den = den;
    }

    public BasicCoreComponentProperty(long bccpId, long bdtId, String definition) {
        this.bccpId = bccpId;
        this.bdtId = bdtId;
        this.definition = definition;
    }

    /*
     * Copy constructor
     */
    public BasicCoreComponentProperty(BasicCoreComponentProperty bccp) {
        this.bccpId = bccp.getBccpId();
        this.guid = bccp.getGuid();
        this.propertyTerm = bccp.getPropertyTerm();
        this.representationTerm = bccp.getRepresentationTerm();
        this.bdtId = bccp.getBdtId();
        this.den = bccp.getDen();
        this.definition = bccp.getDefinition();
        this.module = bccp.getModule();
        this.namespaceId = bccp.getNamespaceId();
        this.deprecated = bccp.isDeprecated();
        this.createdBy = bccp.getCreatedBy();
        this.ownerUserId = bccp.getOwnerUserId();
        this.lastUpdatedBy = bccp.getLastUpdatedBy();
        this.creationTimestamp = bccp.getCreationTimestamp();
        this.lastUpdateTimestamp = bccp.getLastUpdateTimestamp();
        this.state = bccp.getState();
        this.revisionNum = bccp.getRevisionNum();
        this.revisionTrackingNum = bccp.getRevisionTrackingNum();
        this.revisionAction = bccp.getRevisionAction();
        this.releaseId = bccp.getReleaseId();
        this.currentBccpId = bccp.getCurrentBccpId();
        this.nillable = bccp.isNillable();
        this.defaultValue = bccp.getDefaultValue();
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

    public long getBccpId() {
        return bccpId;
    }

    public void setBccpId(long bccpId) {
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

    public long getBdtId() {
        return bdtId;
    }

    public void setBdtId(long bdtId) {
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

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public long getNamespaceId() {
        return (namespaceId == null) ? 0L : namespaceId;
    }

    public void setNamespaceId(long namespaceId) {
        this.namespaceId = namespaceId;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
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

    public long getReleaseId() {
        return (releaseId == null) ? 0L : releaseId;
    }

    public void setReleaseId(long releaseId) {
        this.releaseId = releaseId;
    }

    public long getCurrentBccpId() {
        return (currentBccpId == null) ? 0L : currentBccpId;
    }

    public void setCurrentBccpId(long currentBccpId) {
        this.currentBccpId = currentBccpId;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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
        if (nillable != that.nillable) return false;
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
        if (currentBccpId != null ? !currentBccpId.equals(that.currentBccpId) : that.currentBccpId != null)
            return false;
        return defaultValue != null ? defaultValue.equals(that.defaultValue) : that.defaultValue == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (bccpId ^ (bccpId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (representationTerm != null ? representationTerm.hashCode() : 0);
        result = 31 * result + (int) (bdtId ^ (bdtId >>> 32));
        result = 31 * result + (den != null ? den.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + (namespaceId != null ? namespaceId.hashCode() : 0);
        result = 31 * result + (deprecated ? 1 : 0);
        result = 31 * result + (int) (createdBy ^ (createdBy >>> 32));
        result = 31 * result + (int) (ownerUserId ^ (ownerUserId >>> 32));
        result = 31 * result + (int) (lastUpdatedBy ^ (lastUpdatedBy >>> 32));
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + state;
        result = 31 * result + revisionNum;
        result = 31 * result + revisionTrackingNum;
        result = 31 * result + revisionAction;
        result = 31 * result + (releaseId != null ? releaseId.hashCode() : 0);
        result = 31 * result + (currentBccpId != null ? currentBccpId.hashCode() : 0);
        result = 31 * result + (nillable ? 1 : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
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
                ", module=" + module +
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
                ", nillable=" + nillable +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
