package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.repository.entity.converter.CoreComponentStateConverter;
import org.oagi.srt.repository.entity.converter.RevisionActionConverter;
import org.oagi.srt.repository.entity.listener.PersistEventListener;
import org.oagi.srt.repository.entity.listener.TimestampAwareEventListener;
import org.oagi.srt.repository.entity.listener.UpdateEventListener;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "asccp")
public class AssociationCoreComponentProperty implements
        CoreComponentProperty, NamespaceAware, RevisionAware {

    public static final String SEQUENCE_NAME = "ASCCP_ID_SEQ";

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
    private long asccpId;

    @Column(nullable = false, length = 41, updatable = false)
    private String guid;

    @Column(nullable = false)
    private String propertyTerm;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(length = 100)
    private String definitionSource;

    @Column
    private Long roleOfAccId;
    @Transient
    private AggregateCoreComponent roleOfAcc;

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
    @Convert(attributeName = "state", converter = CoreComponentStateConverter.class)
    private CoreComponentState state;

    @Column
    private Long moduleId;

    @Column
    private Long namespaceId;

    @Column(nullable = false)
    private boolean reusableIndicator;

    @Column(name = "is_deprecated", nullable = false)
    private boolean deprecated;

    @Column(nullable = false)
    private int revisionNum;

    @Column(nullable = false)
    private int revisionTrackingNum;

    @Column
    @Convert(attributeName = "revisionAction", converter = RevisionActionConverter.class)
    private RevisionAction revisionAction;

    @Column
    private Long releaseId;

    @Column
    private Long currentAsccpId;

    @Column(name = "is_nillable", nullable = false)
    private boolean nillable;

    public AssociationCoreComponentProperty() {
        init();
    }

    @Override
    public long getId() {
        return getAsccpId();
    }

    @Override
    public void setId(long id) {
        setAsccpId(id);
    }

    @Override
    public String tableName() {
        return "ASCCP";
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
        if (roleOfAcc != null && !StringUtils.isEmpty(propertyTerm)) {
            setDen(propertyTerm + ". " + roleOfAcc.getObjectClassTerm());
        }
    }

    public void setPropertyTerm(String propertyTerm, AggregateCoreComponent roleOfAcc) {
        setPropertyTerm(propertyTerm);
        setRoleOfAcc(roleOfAcc);
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

    public long getRoleOfAccId() {
        return (roleOfAccId != null) ? roleOfAccId : 0L;
    }

    public void setRoleOfAccId(Long roleOfAccId) {
        this.roleOfAccId = roleOfAccId;
    }

    public void setRoleOfAcc(AggregateCoreComponent roleOfAcc) {
        this.roleOfAcc = roleOfAcc;
        if (roleOfAcc != null) {
            setRoleOfAccId(roleOfAcc.getAccId());
        }
        if (!StringUtils.isEmpty(propertyTerm)) {
            setDen(propertyTerm + ". " + roleOfAcc.getObjectClassTerm());
        }
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

    public CoreComponentState getState() {
        return state;
    }

    public void setState(CoreComponentState state) {
        this.state = state;
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

    public RevisionAction getRevisionAction() {
        return revisionAction;
    }

    public void setRevisionAction(RevisionAction revisionAction) {
        this.revisionAction = revisionAction;
    }

    public long getReleaseId() {
        return (releaseId == null) ? 0L : releaseId;
    }

    public void setReleaseId(Long releaseId) {
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

    public AssociationCoreComponentProperty clone() {
        AssociationCoreComponentProperty clone = new AssociationCoreComponentProperty();
        clone.setGuid(this.guid);
        clone.setPropertyTerm(this.propertyTerm);
        clone.setDefinition(this.definition);
        clone.setDefinitionSource(this.definitionSource);
        clone.setRoleOfAccId(this.roleOfAccId);
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
        clone.setNamespaceId(this.namespaceId);
        clone.setReusableIndicator(this.reusableIndicator);
        clone.setDeprecated(this.deprecated);
        clone.setRevisionNum(this.revisionNum);
        clone.setRevisionTrackingNum(this.revisionTrackingNum);
        clone.setRevisionAction(this.revisionAction);
        if (this.releaseId != null) {
            clone.setReleaseId(this.releaseId);
        }
        if (this.currentAsccpId != null) {
            clone.setCurrentAsccpId(this.currentAsccpId);
        }
        clone.setNillable(this.nillable);
        clone.afterLoaded();
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssociationCoreComponentProperty that = (AssociationCoreComponentProperty) o;

        if (asccpId != 0L && asccpId == that.asccpId) return true;
        if (guid != null) {
            if (guid.equals(that.guid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (asccpId ^ (asccpId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (definitionSource != null ? definitionSource.hashCode() : 0);
        result = 31 * result + (int) (roleOfAccId ^ (roleOfAccId >>> 32));
        result = 31 * result + (den != null ? den.hashCode() : 0);
        result = 31 * result + (int) (createdBy ^ (createdBy >>> 32));
        result = 31 * result + (int) (ownerUserId ^ (ownerUserId >>> 32));
        result = 31 * result + (int) (lastUpdatedBy ^ (lastUpdatedBy >>> 32));
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (moduleId != null ? moduleId.hashCode() : 0);
        result = 31 * result + (namespaceId != null ? namespaceId.hashCode() : 0);
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
                ", definitionSource='" + definitionSource + '\'' +
                ", roleOfAccId=" + roleOfAccId +
                ", den='" + den + '\'' +
                ", createdBy=" + createdBy +
                ", ownerUserId=" + ownerUserId +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", state=" + state +
                ", moduleId=" + moduleId +
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
                AssociationCoreComponentProperty asccp = (AssociationCoreComponentProperty) object;
                asccp.afterLoaded();
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
