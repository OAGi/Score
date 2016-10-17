package org.oagi.srt.repository.entity;
import org.hibernate.annotations.CacheConcurrencyStrategy;
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

@Entity
@Table(name = "bbie_sc")
@org.hibernate.annotations.Cache(region = "", usage = CacheConcurrencyStrategy.READ_WRITE)
public class BasicBusinessInformationEntitySupplementaryComponent implements Serializable, IdEntity {

    public static final String SEQUENCE_NAME = "BBIE_SC_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1000)
    private long bbieScId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false)
    private long bbieId;
    @Transient
    private BasicBusinessInformationEntity bbie;

    @Column(nullable = false)
    private long dtScId;
    @Transient
    private DataTypeSupplementaryComponent dtSc;

    @Column
    private Long dtScPriRestriId;
    @Transient
    private BusinessDataTypeSupplementaryComponentPrimitiveRestriction dtScPriRestri;

    @Column
    private Long codeListId;
    @Transient
    private CodeList codeList;

    @Column
    private Long agencyIdListId;
    @Transient
    private AgencyIdList agencyIdList;

    @Column(nullable = false)
    private int cardinalityMin;

    @Column
    private int cardinalityMax;

    @Column
    private String defaultValue;

    @Column
    private String fixedValue;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(length = 225)
    private String remark;

    @Column(length = 225)
    private String bizTerm;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @Column(nullable = false)
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
        this.dtSc = dtSc;
    }

    public long getDtScPriRestriId() {
        return (dtScPriRestriId == null) ? 0L : dtScPriRestriId;
    }

    public void setDtScPriRestriId(Long dtScPriRestriId) {
        this.dtScPriRestriId = dtScPriRestriId;
    }

    public void setDtScPriRestri(BusinessDataTypeSupplementaryComponentPrimitiveRestriction dtScPriRestri) {
        this.dtScPriRestri = dtScPriRestri;
    }

    public long getCodeListId() {
        return (codeListId == null) ? 0L : codeListId;
    }

    public void setCodeListId(Long codeListId) {
        this.codeListId = codeListId;
    }

    public void setCodeList(CodeList codeList) {
        this.codeList = codeList;
    }

    public long getAgencyIdListId() {
        return (agencyIdListId == null) ? 0L : agencyIdListId;
    }

    public void setAgencyIdListId(Long agencyIdListId) {
        this.agencyIdListId = agencyIdListId;
    }

    public void setAgencyIdList(AgencyIdList agencyIdList) {
        this.agencyIdList = agencyIdList;
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

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        if (!StringUtils.isEmpty(definition)) {
            this.definition = definition;
        }
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
        result = 31 * result + (dtScPriRestriId != null ? dtScPriRestriId.hashCode() : 0);
        result = 31 * result + (codeListId != null ? codeListId.hashCode() : 0);
        result = 31 * result + (agencyIdListId != null ? agencyIdListId.hashCode() : 0);
        result = 31 * result + cardinalityMin;
        result = 31 * result + cardinalityMax;
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (fixedValue != null ? fixedValue.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
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
                ", definition='" + definition + '\'' +
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
                BasicBusinessInformationEntitySupplementaryComponent bbiesc = (BasicBusinessInformationEntitySupplementaryComponent) object;
                if (bbiesc.bbie != null) {
                    bbiesc.setBbieId(bbiesc.bbie.getBbieId());
                }
                if (bbiesc.dtSc != null) {
                    bbiesc.setDtScId(bbiesc.dtSc.getDtScId());
                }
                if (bbiesc.dtScPriRestriId != null) {
                    bbiesc.setDtScPriRestriId(bbiesc.dtScPriRestri.getBdtScPriRestriId());
                }
                if (bbiesc.codeListId != null) {
                    bbiesc.setCodeListId(bbiesc.codeList.getCodeListId());
                }
                if (bbiesc.agencyIdList != null) {
                    bbiesc.setAgencyIdListId(bbiesc.agencyIdListId);
                }
                if (bbiesc.bbie != null) {
                    bbiesc.setBbieId(bbiesc.bbie.getBbieId());
                }
                if (bbiesc.ownerTopLevelAbie != null) {
                    bbiesc.setOwnerTopLevelAbieId(bbiesc.ownerTopLevelAbie.getTopLevelAbieId());
                }
            }
            @Override
            public void onPostPersist(Object object) {
            }
        });
        addUpdateEventListener(timestampAwareEventListener);
        addUpdateEventListener(new UpdateEventListener() {
            @Override
            public void onPreUpdate(Object object) {
                BasicBusinessInformationEntitySupplementaryComponent bbiesc = (BasicBusinessInformationEntitySupplementaryComponent) object;
                if (bbiesc.dtScPriRestriId != null) {
                    bbiesc.setDtScPriRestriId(bbiesc.dtScPriRestri.getBdtScPriRestriId());
                }
                if (bbiesc.codeListId != null) {
                    bbiesc.setCodeListId(bbiesc.codeList.getCodeListId());
                }
                if (bbiesc.agencyIdList != null) {
                    bbiesc.setAgencyIdListId(bbiesc.agencyIdListId);
                }
            }
            @Override
            public void onPostUpdate(Object object) {

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
}
