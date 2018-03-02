package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.repository.entity.converter.BasicCoreComponentEntityTypeConverter;
import org.oagi.srt.repository.entity.converter.CoreComponentStateConverter;
import org.oagi.srt.repository.entity.converter.RevisionActionConverter;
import org.oagi.srt.repository.entity.listener.PersistEventListener;
import org.oagi.srt.repository.entity.listener.TimestampAwareEventListener;
import org.oagi.srt.repository.entity.listener.UpdateEventListener;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "bcc")
public class BasicCoreComponent implements
        CoreComponentRelation, RevisionAware {

    public static final String SEQUENCE_NAME = "BCC_ID_SEQ";

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
    private long bccId;

    @Column(nullable = false, length = 41, updatable = false)
    private String guid;

    @Column(nullable = false)
    private int cardinalityMin;

    @Column
    private int cardinalityMax;

    @Column(nullable = false)
    private long fromAccId;

    @Column(nullable = false)
    private long toBccpId;

    @Column
    private int seqKey;

    @Column
    @Convert(attributeName = "entityType", converter = BasicCoreComponentEntityTypeConverter.class)
    private BasicCoreComponentEntityType entityType;

    @Column(nullable = false)
    private String den;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(length = 100)
    private String definitionSource;

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
    private Long currentBccId;

    @Column(name = "is_deprecated", nullable = false)
    private boolean deprecated;

    @Column(name = "is_nillable", nullable = false)
    private boolean nillable;

    @Column
    private String defaultValue;

    public BasicCoreComponent() {
        init();
    }

    @Override
    public long getId() {
        return getBccId();
    }

    @Override
    public void setId(long id) {
        setBccId(id);
    }

    @Override
    public String tableName() {
        return "BCC";
    }

    public long getBccId() {
        return bccId;
    }

    public void setBccId(long bccId) {
        this.bccId = bccId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
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

    public long getFromAccId() {
        return fromAccId;
    }

    public void setFromAccId(long fromAccId) {
        if (fromAccId > 0) {
            this.fromAccId = fromAccId;
        }
    }

    public long getToBccpId() {
        return toBccpId;
    }

    public void setToBccpId(long toBccpId) {
        if (toBccpId > 0) {
            this.toBccpId = toBccpId;
        }
    }

    public int getSeqKey() {
        return seqKey;
    }

    public void setSeqKey(int seqKey) {
        this.seqKey = seqKey;
    }

    public BasicCoreComponentEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(BasicCoreComponentEntityType entityType) {
        this.entityType = entityType;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public void setDen(AggregateCoreComponent acc, BasicCoreComponentProperty bccp) {
        setDen(acc.getObjectClassTerm() + ". " + bccp.getDen());
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

    public void setReleaseId(Long releaseId) {
        this.releaseId = releaseId;
    }

    public long getCurrentBccId() {
        return (currentBccId == null) ? 0L : currentBccId;
    }

    public void setCurrentBccId(long currentBccId) {
        this.currentBccId = currentBccId;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
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

    public BasicCoreComponent clone() {
        BasicCoreComponent clone = new BasicCoreComponent();
        clone.setGuid(this.guid);
        clone.setCardinalityMin(this.cardinalityMin);
        clone.setCardinalityMax(this.cardinalityMax);
        clone.setFromAccId(this.fromAccId);
        clone.setToBccpId(this.toBccpId);
        clone.setSeqKey(this.seqKey);
        clone.setEntityType(this.entityType);
        clone.setDen(this.den);
        clone.setDefinition(this.definition);
        clone.setDefinitionSource(this.definitionSource);
        clone.setCreatedBy(this.createdBy);
        clone.setLastUpdatedBy(this.lastUpdatedBy);
        clone.setOwnerUserId(this.ownerUserId);
        Date timestamp = new Date();
        clone.setCreationTimestamp(timestamp);
        clone.setLastUpdateTimestamp(timestamp);
        clone.setState(this.state);
        clone.setDeprecated(this.deprecated);
        clone.setRevisionNum(this.revisionNum);
        clone.setRevisionTrackingNum(this.revisionTrackingNum);
        clone.setRevisionAction(this.revisionAction);
        if (this.releaseId != null) {
            clone.setReleaseId(this.releaseId);
        }
        if (this.currentBccId != null) {
            clone.setCurrentBccId(this.currentBccId);
        }
        clone.setNillable(this.nillable);
        clone.setDefaultValue(this.defaultValue);
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicCoreComponent that = (BasicCoreComponent) o;

        if (bccId != 0L && bccId == that.bccId) return true;
        if (guid != null) {
            if (guid.equals(that.guid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (bccId ^ (bccId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + cardinalityMin;
        result = 31 * result + cardinalityMax;
        result = 31 * result + (int) (fromAccId ^ (fromAccId >>> 32));
        result = 31 * result + (int) (toBccpId ^ (toBccpId >>> 32));
        result = 31 * result + seqKey;
        result = 31 * result + (entityType != null ? entityType.hashCode() : 0);
        result = 31 * result + (den != null ? den.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (definitionSource != null ? definitionSource.hashCode() : 0);
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
        result = 31 * result + (currentBccId != null ? currentBccId.hashCode() : 0);
        result = 31 * result + (deprecated ? 1 : 0);
        result = 31 * result + (nillable ? 1 : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BasicCoreComponent{" +
                "bccId=" + bccId +
                ", guid='" + guid + '\'' +
                ", cardinalityMin=" + cardinalityMin +
                ", cardinalityMax=" + cardinalityMax +
                ", fromAccId=" + fromAccId +
                ", toBccpId=" + toBccpId +
                ", seqKey=" + seqKey +
                ", entityType=" + entityType +
                ", den='" + den + '\'' +
                ", definition='" + definition + '\'' +
                ", definitionSource='" + definitionSource + '\'' +
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
                ", currentBccId=" + currentBccId +
                ", deprecated=" + deprecated +
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
                BasicCoreComponent bcc = (BasicCoreComponent) object;
                bcc.afterLoaded();
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
