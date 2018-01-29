package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.repository.entity.listener.PersistEventListener;
import org.oagi.srt.repository.entity.listener.UpdateEventListener;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "dt_sc")
public class DataTypeSupplementaryComponent implements IEntity {

    public static final String SEQUENCE_NAME = "DT_SC_ID_SEQ";

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
    private long dtScId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(length = 60)
    private String propertyTerm;

    @Column(length = 20)
    private String representationTerm;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(length = 100)
    private String definitionSource;

    @Column(nullable = false)
    private long ownerDtId;

    @Column(nullable = false)
    private int cardinalityMin;

    @Column
    private int cardinalityMax;

    @Column
    private Long basedDtScId;

    public DataTypeSupplementaryComponent() {
        init();
    }

    @Override
    public long getId() {
        return getDtScId();
    }

    @Override
    public void setId(long id) {
        setDtScId(id);
    }

    @Override
    public String tableName() {
        return "DT_SC";
    }

    public long getDtScId() {
        return dtScId;
    }

    public void setDtScId(long dtScId) {
        this.dtScId = dtScId;
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

    public long getOwnerDtId() {
        return ownerDtId;
    }

    public void setOwnerDtId(long ownerDtId) {
        this.ownerDtId = ownerDtId;
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

    public long getBasedDtScId() {
        return (basedDtScId == null) ? 0L : basedDtScId;
    }

    public void setBasedDtScId(long basedDtScId) {
        this.basedDtScId = basedDtScId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataTypeSupplementaryComponent that = (DataTypeSupplementaryComponent) o;

        if (dtScId != 0L && dtScId == that.dtScId) return true;
        if (guid != null) {
            if (guid.equals(that.guid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (dtScId ^ (dtScId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (representationTerm != null ? representationTerm.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (definitionSource != null ? definitionSource.hashCode() : 0);
        result = 31 * result + (int) (ownerDtId ^ (ownerDtId >>> 32));
        result = 31 * result + cardinalityMin;
        result = 31 * result + cardinalityMax;
        result = 31 * result + (basedDtScId != null ? basedDtScId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DataTypeSupplementaryComponent{" +
                "dtScId=" + dtScId +
                ", guid='" + guid + '\'' +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", representationTerm='" + representationTerm + '\'' +
                ", definition='" + definition + '\'' +
                ", definitionSource='" + definitionSource + '\'' +
                ", ownerDtId=" + ownerDtId +
                ", cardinalityMin=" + cardinalityMin +
                ", cardinalityMax=" + cardinalityMax +
                ", basedDtScId=" + basedDtScId +
                '}';
    }

    @Transient
    private transient List<PersistEventListener> persistEventListeners;

    @Transient
    private transient List<UpdateEventListener> updateEventListeners;

    private void init() {
        addPersistEventListener(new PersistEventListener() {
            @Override
            public void onPrePersist(Object object) {
            }

            @Override
            public void onPostPersist(Object object) {
                DataTypeSupplementaryComponent dtSc = (DataTypeSupplementaryComponent) object;
                dtSc.afterLoaded();
            }
        });
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

    public DataTypeSupplementaryComponent clone() {
        DataTypeSupplementaryComponent clone = new DataTypeSupplementaryComponent();

        clone.propertyTerm = this.propertyTerm;
        clone.representationTerm = this.representationTerm;
        clone.ownerDtId = this.ownerDtId;
        clone.cardinalityMin = this.cardinalityMin;
        clone.cardinalityMax = this.cardinalityMax;
        clone.basedDtScId = this.dtScId;

        return clone;
    }
}
