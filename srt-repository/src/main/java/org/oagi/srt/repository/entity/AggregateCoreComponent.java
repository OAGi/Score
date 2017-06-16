package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.repository.entity.converter.CoreComponentStateConverter;
import org.oagi.srt.repository.entity.converter.OagisComponentTypeConverter;
import org.oagi.srt.repository.entity.converter.RevisionActionConverter;
import org.oagi.srt.repository.entity.listener.PersistEventListener;
import org.oagi.srt.repository.entity.listener.TimestampAwareEventListener;
import org.oagi.srt.repository.entity.listener.UpdateEventListener;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "acc")
public class AggregateCoreComponent
        implements CoreComponent, CreatorModifierAware, TimestampAware, NamespaceAware, Serializable {

    public static final String SEQUENCE_NAME = "ACC_ID_SEQ";

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
    private long accId;

    @Column(nullable = false, length = 41, updatable = false)
    private String guid;

    @Column(nullable = false, length = 100)
    private String objectClassTerm;

    @Column(nullable = false, length = 200)
    private String den;

    @Column
    private Long definitionId;
    @Transient
    private Definition definition;

    @Column
    private Long basedAccId;

    @Column(length = 100)
    private String objectClassQualifier;

    @Column
    @Convert(attributeName = "oagisComponentType", converter = OagisComponentTypeConverter.class)
    private OagisComponentType oagisComponentType;

    @Column
    private Long moduleId;

    @Column
    private Long namespaceId;

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
    private RevisionAction revisionAction;

    @Column
    private Long releaseId;

    @Column
    private Long currentAccId;

    @Column(name = "is_deprecated", nullable = false)
    private boolean deprecated;

    @Column(name = "is_abstract", nullable = false)
    private boolean isAbstract;

    public AggregateCoreComponent() {
        init();
    }

    public AggregateCoreComponent(long accId, String den) {
        init();

        this.accId = accId;
        this.den = den;
    }

    @Override
    public long getId() {
        return getAccId();
    }

    @Override
    public void setId(long id) {
        setAccId(id);
    }

    @Override
    public String tableName() {
        return "ACC";
    }

    public long getAccId() {
        return accId;
    }

    public void setAccId(long accId) {
        this.accId = accId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getObjectClassTerm() {
        return objectClassTerm;
    }

    public void setObjectClassTerm(String objectClassTerm) {
        this.objectClassTerm = objectClassTerm;
        setDen(objectClassTerm + ". Details");
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public Long getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(Long definitionId) {
        this.definitionId = definitionId;
    }

    public String getDefinition() {
        return (this.definition != null) ? this.definition.getDefinition() : null;
    }

    public Definition getRawDefinition() {
        return this.definition;
    }

    public void setRawDefinition(Definition definition) {
        this.definition = definition;
    }

    public void setDefinition(String definition) {
        if (definition != null) {
            definition = definition.trim();
        }
        if (StringUtils.isEmpty(definition)) {
            return;
        }

        if (this.definition == null) {
            this.definition = new Definition();
        }
        this.definition.setDefinition(definition);
    }

    public long getBasedAccId() {
        return (basedAccId == null) ? 0L : basedAccId;
    }

    public void setBasedAccId(Long basedAccId) {
        this.basedAccId = basedAccId;
    }

    public String getObjectClassQualifier() {
        return objectClassQualifier;
    }

    public void setObjectClassQualifier(String objectClassQualifier) {
        this.objectClassQualifier = objectClassQualifier;
    }

    public OagisComponentType getOagisComponentType() {
        return oagisComponentType;
    }

    public void setOagisComponentType(OagisComponentType oagisComponentType) {
        this.oagisComponentType = oagisComponentType;
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

    public long getCurrentAccId() {
        return (currentAccId == null) ? 0L : currentAccId;
    }

    public void setCurrentAccId(long currentAccId) {
        this.currentAccId = currentAccId;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    /*
     * An 'abstract' property is accessed by JSF, ELException will be thrown
     * because it's recognized as a keyword of Java.
     * Hence, this alias uses instead of an original at that point.
     */
    public boolean isAbstractComponent() {
        return isAbstract();
    }

    public void setAbstractComponent(boolean abstractComponent) {
        setAbstract(abstractComponent);
    }

    public boolean isExtension() {
        return OagisComponentType.Extension == getOagisComponentType();
    }

    public boolean isGlobalExtension() {
        return isExtension() && "All Extension".equals(getObjectClassTerm());
    }

    public AggregateCoreComponent clone(boolean shallowCopy) {
        AggregateCoreComponent clone = new AggregateCoreComponent();
        clone.setGuid(this.guid);
        clone.setObjectClassTerm(this.objectClassTerm);
        clone.setDen(this.den);

        if (shallowCopy) {
            clone.definitionId = this.definitionId;
        } else {
            clone.definition = (definition != null) ? definition.clone() : null;
        }

        if (this.basedAccId != null) {
            clone.setBasedAccId(this.basedAccId);
        }
        clone.setObjectClassQualifier(this.objectClassQualifier);
        clone.setOagisComponentType(this.oagisComponentType);
        if (this.moduleId != null) {
            clone.setModuleId(this.moduleId);
        }
        if (this.namespaceId != null) {
            clone.setNamespaceId(this.namespaceId);
        }
        clone.setCreatedBy(this.createdBy);
        clone.setOwnerUserId(this.ownerUserId);
        clone.setLastUpdatedBy(this.lastUpdatedBy);
        Date timestamp = new Date();
        clone.setCreationTimestamp(timestamp);
        clone.setLastUpdateTimestamp(timestamp);
        clone.setState(this.state);
        clone.setRevisionNum(this.revisionNum);
        clone.setRevisionTrackingNum(this.revisionTrackingNum);
        clone.setRevisionAction(this.revisionAction);
        if (this.releaseId != null) {
            clone.setReleaseId(this.releaseId);
        }
        if (this.currentAccId != null) {
            clone.setCurrentAccId(this.currentAccId);
        }
        clone.setDeprecated(this.deprecated);
        clone.setAbstract(this.isAbstract);
        clone.afterLoaded();
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AggregateCoreComponent that = (AggregateCoreComponent) o;

        if (accId != 0L && accId == that.accId) return true;
        if (guid != null) {
            if (guid.equals(that.guid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (accId ^ (accId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (objectClassTerm != null ? objectClassTerm.hashCode() : 0);
        result = 31 * result + (den != null ? den.hashCode() : 0);
        result = 31 * result + (definitionId != null ? definitionId.hashCode() : 0);
        result = 31 * result + (basedAccId != null ? basedAccId.hashCode() : 0);
        result = 31 * result + (objectClassQualifier != null ? objectClassQualifier.hashCode() : 0);
        result = 31 * result + (oagisComponentType != null ? oagisComponentType.hashCode() : 0);
        result = 31 * result + (moduleId != null ? moduleId.hashCode() : 0);
        result = 31 * result + (namespaceId != null ? namespaceId.hashCode() : 0);
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
        result = 31 * result + (currentAccId != null ? currentAccId.hashCode() : 0);
        result = 31 * result + (deprecated ? 1 : 0);
        result = 31 * result + (isAbstract ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AggregateCoreComponent{" +
                "accId=" + accId +
                ", guid='" + guid + '\'' +
                ", objectClassTerm='" + objectClassTerm + '\'' +
                ", den='" + den + '\'' +
                ", definitionId='" + definitionId + '\'' +
                ", basedAccId=" + basedAccId +
                ", objectClassQualifier='" + objectClassQualifier + '\'' +
                ", oagisComponentType=" + oagisComponentType +
                ", moduleId=" + moduleId +
                ", namespaceId=" + namespaceId +
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
                ", currentAccId=" + currentAccId +
                ", deprecated=" + deprecated +
                ", isAbstract=" + isAbstract +
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
                AggregateCoreComponent acc = (AggregateCoreComponent) object;
                acc.afterLoaded();

                if (acc.definition != null) {
                    acc.definition.setRefId(getId());
                    acc.definition.setRefTableName(tableName());
                }
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
