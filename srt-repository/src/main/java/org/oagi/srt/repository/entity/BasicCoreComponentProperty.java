package org.oagi.srt.repository.entity;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.entity.converter.CoreComponentStateConverter;
import org.oagi.srt.repository.entity.converter.RevisionActionConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "bccp")
@org.hibernate.annotations.Cache(region = "", usage = CacheConcurrencyStrategy.READ_WRITE)
public class BasicCoreComponentProperty
        implements CoreComponentProperty, Serializable, Cloneable {

    public static final String SEQUENCE_NAME = "BCCP_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
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
    @Convert(attributeName = "state", converter = CoreComponentStateConverter.class)
    private CoreComponentState state;

    @Column(nullable = false)
    private int revisionNum;

    @Column(nullable = false)
    private int revisionTrackingNum;

    @Column
    @Convert(attributeName = "revisionAction", converter = RevisionActionConverter.class)
    private RevisionAction revisionAction = RevisionAction.Insert;

    @Column
    private Long releaseId;

    @Column
    private Long currentBccpId;

    @Column(name = "is_nillable", nullable = false)
    private boolean nillable;

    @Column
    private String defaultValue;

    public BasicCoreComponentProperty() {
    }

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

    public CoreComponentState getState() {
        return state;
    }

    public void setState(CoreComponentState state) {
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

    public RevisionAction getRevisionAction() {
        return revisionAction;
    }

    public void setRevisionAction(RevisionAction revisionAction) {
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
    public BasicCoreComponentProperty clone() {
        BasicCoreComponentProperty clone = new BasicCoreComponentProperty();
        clone.setGuid(Utility.generateGUID());
        clone.setPropertyTerm(this.propertyTerm);
        clone.setRepresentationTerm(this.representationTerm);
        clone.setBdtId(this.bdtId);
        clone.setDefinition(this.definition);
        clone.setDen(this.den);
        clone.setCreatedBy(this.createdBy);
        clone.setLastUpdatedBy(this.lastUpdatedBy);
        clone.setOwnerUserId(this.ownerUserId);
        Date timestamp = new Date();
        clone.setCreationTimestamp(timestamp);
        clone.setLastUpdateTimestamp(timestamp);
        clone.setState(this.state);
        clone.setModule(this.module);
        clone.setNamespaceId(this.namespaceId);
        clone.setDeprecated(this.deprecated);
        clone.setRevisionNum(this.revisionNum);
        clone.setRevisionTrackingNum(this.revisionTrackingNum);
        clone.setRevisionAction(this.revisionAction);
        clone.setReleaseId(this.releaseId);
        clone.setCurrentBccpId(this.currentBccpId);
        clone.setNillable(this.nillable);
        clone.setDefaultValue(this.defaultValue);
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicCoreComponentProperty that = (BasicCoreComponentProperty) o;

        if (bccpId != 0L && bccpId == that.bccpId) return true;
        if (guid != null) {
            if (guid.equals(that.guid)) {
                return true;
            }
        }
        return false;
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
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + revisionNum;
        result = 31 * result + revisionTrackingNum;
        result = 31 * result + (revisionAction != null ? revisionAction.hashCode() : 0);
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
