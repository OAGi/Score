package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.repository.entity.converter.CoreComponentStateConverter;
import org.oagi.srt.repository.entity.converter.RevisionActionConverter;
import org.oagi.srt.repository.entity.listener.PersistEventListener;
import org.oagi.srt.repository.entity.listener.TimestampAwareEventListener;
import org.oagi.srt.repository.entity.listener.UpdateEventListener;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "bccp")
public class BasicCoreComponentProperty
        implements CoreComponentProperty, CreatorModifierAware, TimestampAware, NamespaceAware, Serializable, RevisionAware {

    public static final String SEQUENCE_NAME = "BCCP_ID_SEQ";

    @Id
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.oagi.srt.repository.support.jpa.ByDialectIdentifierGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1")
            }
    )
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.AUTO)
    private long bccpId;

    @Column(nullable = false, length = 41, updatable = false)
    private String guid;

    @Column(nullable = false, length = 60)
    private String propertyTerm;

    @Column(nullable = false, length = 20)
    private String representationTerm;

    @Column(nullable = false)
    private long bdtId;
    @Transient
    private DataType bdt;

    @Column(nullable = false, length = 200)
    private String den;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(length = 100)
    private String definitionSource;

    @Column
    private Long moduleId;

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
        init();
    }

    public BasicCoreComponentProperty(long bccpId, String den) {
        init();

        this.bccpId = bccpId;
        this.den = den;
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
        this.definitionSource = bccp.getDefinitionSource();
        this.moduleId = bccp.getModuleId();
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

    @Override
    public long getId() {
        return getBccpId();
    }

    @Override
    public void setId(long id) {
        setBccpId(id);
    }

    @Override
    public String tableName() {
        return "BCCP";
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
        if (bdt != null && !StringUtils.isEmpty(propertyTerm)) {
            setDen(propertyTerm + ". " + bdt.getDataTypeTerm());
        }
    }

    public void setPropertyTerm(String propertyTerm, DataType bdt) {
        setPropertyTerm(propertyTerm);
        setBdt(bdt);
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

    public void setBdt(DataType bdt) {
        this.bdt = bdt;
        if (bdt != null) {
            setRepresentationTerm(bdt.getDataTypeTerm());
        }
        if (!StringUtils.isEmpty(propertyTerm)) {
            setDen(propertyTerm + ". " + bdt.getDataTypeTerm());
        }
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

    public String getDefinitionSource() {
        return definitionSource;
    }

    public void setDefinitionSource(String definitionSource) {
        this.definitionSource = definitionSource;
    }

    public long getModuleId() {
        return (moduleId == null) ? 0L : moduleId;
    }

    public void setModuleId(long moduleId) {
        if (moduleId > 0L) {
            this.moduleId = moduleId;
        }
    }

    public long getNamespaceId() {
        return (namespaceId == null) ? 0L : namespaceId;
    }

    public void setNamespaceId(Long namespaceId) {
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

    public Long getReleaseId() {
        return (releaseId == null) ? 0L : releaseId;
    }

    public void setReleaseId(Long releaseId) {
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

    public BasicCoreComponentProperty clone() {
        BasicCoreComponentProperty clone = new BasicCoreComponentProperty();
        clone.setGuid(this.guid);
        clone.setPropertyTerm(this.propertyTerm);
        clone.setRepresentationTerm(this.representationTerm);
        clone.setBdtId(this.bdtId);
        clone.setDefinition(this.definition);
        clone.setDefinitionSource(this.definitionSource);
        clone.setDen(this.den);
        clone.setCreatedBy(this.createdBy);
        clone.setLastUpdatedBy(this.lastUpdatedBy);
        clone.setOwnerUserId(this.ownerUserId);
        Date timestamp = new Date();
        clone.setCreationTimestamp(timestamp);
        clone.setLastUpdateTimestamp(timestamp);
        clone.setState(this.state);
        if (this.moduleId != null) {
            clone.setModuleId(this.moduleId);
        }
        if (this.namespaceId != null) {
            clone.setNamespaceId(this.namespaceId);
        }
        clone.setDeprecated(this.deprecated);
        clone.setRevisionNum(this.revisionNum);
        clone.setRevisionTrackingNum(this.revisionTrackingNum);
        clone.setRevisionAction(this.revisionAction);
        if (this.releaseId != null) {
            clone.setReleaseId(this.releaseId);
        }
        if (this.currentBccpId != null) {
            clone.setCurrentBccpId(this.currentBccpId);
        }
        clone.setNillable(this.nillable);
        clone.setDefaultValue(this.defaultValue);
        clone.afterLoaded();
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
        result = 31 * result + (definitionSource != null ? definitionSource.hashCode() : 0);
        result = 31 * result + (moduleId != null ? moduleId.hashCode() : 0);
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
                ", definitionSource='" + definitionSource + '\'' +
                ", moduleId=" + moduleId +
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

    @Transient
    private transient List<PersistEventListener> persistEventListeners;

    @Transient
    private transient List<UpdateEventListener> updateEventListeners;

    private void init() {
        TimestampAwareEventListener timestampAwareEventListener = new TimestampAwareEventListener();
        addPersistEventListener(timestampAwareEventListener);
        addPersistEventListener(new PersistEventListener() {
            @Override
            public void onPrePersist(Object object) {
            }

            @Override
            public void onPostPersist(Object object) {
                BasicCoreComponentProperty bccp = (BasicCoreComponentProperty) object;
                bccp.afterLoaded();
            }
        });
        addUpdateEventListener(timestampAwareEventListener);
    }

    public void addPersistEventListener(PersistEventListener persistEventListener) {
        if (persistEventListener == null) {
            return;
        }
        if (persistEventListeners == null) {
            persistEventListeners = new ArrayList();
        }
        persistEventListeners.add(persistEventListener);
    }

    private Collection<PersistEventListener> getPersistEventListeners() {
        return (persistEventListeners != null) ? persistEventListeners : Collections.emptyList();
    }

    public void addUpdateEventListener(UpdateEventListener updateEventListener) {
        if (updateEventListener == null) {
            return;
        }
        if (updateEventListeners == null) {
            updateEventListeners = new ArrayList();
        }
        updateEventListeners.add(updateEventListener);
    }

    private Collection<UpdateEventListener> getUpdateEventListeners() {
        return (updateEventListeners != null) ? updateEventListeners : Collections.emptyList();
    }

    @PrePersist
    public void prePersist() {
        for (PersistEventListener persistEventListener : getPersistEventListeners()) {
            persistEventListener.onPrePersist(this);
        }
    }

    @PostPersist
    public void postPersist() {
        for (PersistEventListener persistEventListener : getPersistEventListeners()) {
            persistEventListener.onPostPersist(this);
        }
    }

    @PreUpdate
    public void preUpdate() {
        for (UpdateEventListener updateEventListener : getUpdateEventListeners()) {
            updateEventListener.onPreUpdate(this);
        }
    }

    @PostUpdate
    public void postUpdate() {
        for (UpdateEventListener updateEventListener : getUpdateEventListeners()) {
            updateEventListener.onPostUpdate(this);
        }
    }

    @Transient
    private int hashCodeAfterLoaded;

    @PostLoad
    public void afterLoaded() {
        hashCodeAfterLoaded = hashCode();
    }

    public boolean isDirty() {
        return hashCodeAfterLoaded != hashCode();
    }
}
