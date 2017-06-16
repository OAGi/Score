package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.repository.JpaRepositoryDefinitionHelper;
import org.oagi.srt.repository.entity.listener.PersistEventListener;
import org.oagi.srt.repository.entity.listener.TimestampAwareEventListener;
import org.oagi.srt.repository.entity.listener.UpdateEventListener;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.oagi.srt.repository.entity.BasicBusinessInformationEntityRestrictionType.*;

@Entity
@Table(name = "bbie_sc")
public class BasicBusinessInformationEntitySupplementaryComponent
        implements BusinessInformationEntity, Usable, Serializable {

    public static final String SEQUENCE_NAME = "BBIE_SC_ID_SEQ";

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
    private long bbieScId;

    @Column(nullable = false, length = 41, updatable = false)
    private String guid;

    @Column(nullable = false, updatable = false)
    private long bbieId;
    @Transient
    private BasicBusinessInformationEntity bbie;

    @Column(nullable = false, updatable = false)
    private long dtScId;

    @Transient
    private BasicBusinessInformationEntityRestrictionType restrictionType;

    @Column
    private Long dtScPriRestriId;

    @Column
    private Long codeListId;

    @Column
    private Long agencyIdListId;

    @Column(nullable = false)
    private int cardinalityMin;

    @Column
    private int cardinalityMax;

    @Column
    private String defaultValue;

    @Column
    private String fixedValue;

    @Column
    private Long definitionId;
    @Transient
    private Definition definition;

    @Column(length = 225)
    private String remark;

    @Column(length = 225)
    private String bizTerm;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @Column(nullable = false, updatable = false)
    private long ownerTopLevelAbieId;
    @Transient
    private TopLevelAbie ownerTopLevelAbie;

    @Override
    public long getId() {
        return getBbieScId();
    }

    @Override
    public void setId(long id) {
        setBbieScId(id);
    }

    @Override
    public String tableName() {
        return "BBIE_SC";
    }

    public long getBbieScId() {
        return bbieScId;
    }

    public void setBbieScId(long bbieScId) {
        this.bbieScId = bbieScId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public long getBbieId() {
        return bbieId;
    }

    public void setBbieId(long bbieId) {
        this.bbieId = bbieId;
    }

    public BasicBusinessInformationEntity getBbie() {
        return bbie;
    }

    public void setBbie(BasicBusinessInformationEntity bbie) {
        this.bbie = bbie;
    }

    public long getDtScId() {
        return dtScId;
    }

    public void setDtScId(long dtScId) {
        this.dtScId = dtScId;
    }

    public void setDtSc(DataTypeSupplementaryComponent dtSc) {
        if (dtSc != null) {
            setDtScId(dtSc.getDtScId());
        }
    }

    public BasicBusinessInformationEntityRestrictionType getRestrictionType() {
        return restrictionType;
    }

    public void setRestrictionType(BasicBusinessInformationEntityRestrictionType restrictionType) {
        this.restrictionType = restrictionType;
    }

    public long getDtScPriRestriId() {
        return (dtScPriRestriId == null) ? 0L : dtScPriRestriId;
    }

    public void setDtScPriRestriId(Long dtScPriRestriId) {
        this.dtScPriRestriId = dtScPriRestriId;
    }

    public void setDtScPriRestri(BusinessDataTypeSupplementaryComponentPrimitiveRestriction dtScPriRestri) {
        if (dtScPriRestri != null) {
            setDtScPriRestriId(dtScPriRestri.getBdtScPriRestriId());
        }
    }

    public long getCodeListId() {
        return (codeListId == null) ? 0L : codeListId;
    }

    public void setCodeListId(Long codeListId) {
        this.codeListId = codeListId;
    }

    public void setCodeList(CodeList codeList) {
        if (codeList != null) {
            setCodeListId(codeList.getCodeListId());
        }
    }

    public long getAgencyIdListId() {
        return (agencyIdListId == null) ? 0L : agencyIdListId;
    }

    public void setAgencyIdListId(Long agencyIdListId) {
        if (agencyIdListId != null && agencyIdListId > 0L) {
            this.agencyIdListId = agencyIdListId;
        } else {
            this.agencyIdListId = null;
        }
    }

    public void setAgencyIdList(AgencyIdList agencyIdList) {
        if (agencyIdList != null) {
            setAgencyIdListId(agencyIdList.getAgencyIdListId());
        }
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

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getBizTerm() {
        return bizTerm;
    }

    public void setBizTerm(String bizTerm) {
        this.bizTerm = bizTerm;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public long getOwnerTopLevelAbieId() {
        return ownerTopLevelAbieId;
    }

    public void setOwnerTopLevelAbieId(long ownerTopLevelAbieId) {
        this.ownerTopLevelAbieId = ownerTopLevelAbieId;
    }

    public void setOwnerTopLevelAbie(TopLevelAbie ownerTopLevelAbie) {
        this.ownerTopLevelAbie = ownerTopLevelAbie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicBusinessInformationEntitySupplementaryComponent that = (BasicBusinessInformationEntitySupplementaryComponent) o;

        if (bbieScId != 0L && bbieScId == that.bbieScId) return true;
        if (guid != null) {
            if (guid.equals(that.guid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (bbieScId ^ (bbieScId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (int) (bbieId ^ (bbieId >>> 32));
        result = 31 * result + (int) (dtScId ^ (dtScId >>> 32));
        if (restrictionType == Primitive) {
            if (dtScPriRestriId != null) {
                result = 31 * result + dtScPriRestriId.hashCode();
            } else {
                if (codeListId != null) {
                    result = 31 * result + codeListId.hashCode();
                } else if (agencyIdListId != null) {
                    result = 31 * result + agencyIdListId.hashCode();
                } else {
                    result = 31 * result + 0;
                }
            }
        }
        if (restrictionType == Code) {
            if (codeListId != null) {
                result = 31 * result + codeListId.hashCode();
            } else {
                if (dtScPriRestriId != null) {
                    result = 31 * result + dtScPriRestriId.hashCode();
                } else if (agencyIdListId != null) {
                    result = 31 * result + agencyIdListId.hashCode();
                } else {
                    result = 31 * result + 0;
                }
            }
        }
        if (restrictionType == Agency) {
            if (agencyIdListId != null) {
                result = 31 * result + agencyIdListId.hashCode();
            } else {
                if (dtScPriRestriId != null) {
                    result = 31 * result + dtScPriRestriId.hashCode();
                } else if (codeListId != null) {
                    result = 31 * result + codeListId.hashCode();
                } else {
                    result = 31 * result + 0;
                }
            }
        }
        result = 31 * result + cardinalityMin;
        result = 31 * result + cardinalityMax;
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (fixedValue != null ? fixedValue.hashCode() : 0);
        result = 31 * result + (definitionId != null ? definitionId.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (bizTerm != null ? bizTerm.hashCode() : 0);
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + (int) (ownerTopLevelAbieId ^ (ownerTopLevelAbieId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "BasicBusinessInformationEntitySupplementaryComponent{" +
                "bbieScId=" + bbieScId +
                ", guid='" + guid + '\'' +
                ", bbieId=" + bbieId +
                ", dtScId=" + dtScId +
                ", dtScPriRestriId=" + dtScPriRestriId +
                ", codeListId=" + codeListId +
                ", agencyIdListId=" + agencyIdListId +
                ", cardinalityMin=" + cardinalityMin +
                ", cardinalityMax=" + cardinalityMax +
                ", defaultValue='" + defaultValue + '\'' +
                ", fixedValue='" + fixedValue + '\'' +
                ", definitionId='" + definitionId + '\'' +
                ", remark='" + remark + '\'' +
                ", bizTerm='" + bizTerm + '\'' +
                ", used=" + used +
                ", ownerTopLevelAbieId=" + ownerTopLevelAbieId +
                '}';
    }

    @Transient
    private transient List<PersistEventListener> persistEventListeners;

    @Transient
    private transient List<UpdateEventListener> updateEventListeners;

    public BasicBusinessInformationEntitySupplementaryComponent() {
        TimestampAwareEventListener timestampAwareEventListener = new TimestampAwareEventListener();
        addPersistEventListener(timestampAwareEventListener);
        addPersistEventListener(new PersistEventListener() {
            @Override
            public void onPrePersist(Object object) {
                BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                        (BasicBusinessInformationEntitySupplementaryComponent) object;
                if (bbieSc.bbie != null) {
                    bbieSc.setBbieId(bbieSc.bbie.getBbieId());
                }
                if (bbieSc.bbie != null) {
                    bbieSc.setBbieId(bbieSc.bbie.getBbieId());
                }
                if (bbieSc.ownerTopLevelAbie != null) {
                    bbieSc.setOwnerTopLevelAbieId(bbieSc.ownerTopLevelAbie.getTopLevelAbieId());
                }

                ensureRestrictionType();
            }

            @Override
            public void onPostPersist(Object object) {
                BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                        (BasicBusinessInformationEntitySupplementaryComponent) object;
                bbieSc.afterLoaded();

                if (bbieSc.definition != null) {
                    bbieSc.definition.setRefId(getId());
                    bbieSc.definition.setRefTableName(tableName());
                }
            }
        });
        addUpdateEventListener(timestampAwareEventListener);
        addUpdateEventListener(new UpdateEventListener() {
            @Override
            public void onPreUpdate(Object object) {
                ensureRestrictionType();
            }

            @Override
            public void onPostUpdate(Object object) {
                BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                        (BasicBusinessInformationEntitySupplementaryComponent) object;
                bbieSc.afterLoaded();
            }
        });
    }

    private void ensureRestrictionType() {
        switch (restrictionType) {
            case Primitive:
                if (dtScPriRestriId != null && dtScPriRestriId > 0L) {
                    codeListId = null;
                    agencyIdListId = null;
                }

                break;
            case Code:
                if (codeListId != null && codeListId > 0L) {
                    dtScPriRestriId = null;
                    agencyIdListId = null;
                }

                break;
            case Agency:
                if (agencyIdListId != null && agencyIdListId > 0L) {
                    dtScPriRestriId = null;
                    codeListId = null;
                }

                break;
        }
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
        if (dtScPriRestriId != null && dtScPriRestriId > 0L) {
            setRestrictionType(Primitive);
        } else if (codeListId != null && codeListId > 0L) {
            setRestrictionType(Code);
        } else if (agencyIdListId != null && agencyIdListId > 0L) {
            setRestrictionType(Agency);
        } else {
            throw new IllegalStateException();
        }

        hashCodeAfterLoaded = hashCode();
    }

    public boolean isDirty() {
        return hashCodeAfterLoaded != hashCode();
    }

    @Override
    public BasicBusinessInformationEntitySupplementaryComponent clone() {
        BasicBusinessInformationEntitySupplementaryComponent clone =
                new BasicBusinessInformationEntitySupplementaryComponent();
        clone.guid = this.guid;
        clone.bbieId = this.bbieId;
        clone.dtScId = this.dtScId;
        clone.dtScPriRestriId = this.dtScPriRestriId;
        clone.codeListId = this.codeListId;
        clone.agencyIdListId = this.agencyIdListId;
        clone.cardinalityMin = this.cardinalityMin;
        clone.cardinalityMax = this.cardinalityMax;
        clone.defaultValue = this.defaultValue;
        clone.fixedValue = this.fixedValue;
        clone.definition = JpaRepositoryDefinitionHelper.cloneDefinition(this);
        clone.remark = this.remark;
        clone.bizTerm = this.bizTerm;
        clone.used = this.used;
        clone.afterLoaded();
        return clone;
    }
}
