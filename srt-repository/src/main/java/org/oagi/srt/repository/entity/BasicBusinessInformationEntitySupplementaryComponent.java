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
@org.hibernate.annotations.Cache(region = "", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class BasicBusinessInformationEntitySupplementaryComponent implements Serializable, IdEntity {

    public static final String SEQUENCE_NAME = "BBIE_SC_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1000)
    private long bbieScId;

    @Column(nullable = false, length = 41)
    private String guid;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bbie_id", nullable = false)
    private BasicBusinessInformationEntity bbie;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dt_sc_id", nullable = false)
    private DataTypeSupplementaryComponent dtSc;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dt_sc_pri_restri_id")
    private BusinessDataTypeSupplementaryComponentPrimitiveRestriction dtScPriRestri;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "code_list_id")
    private CodeList codeList;

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

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(length = 225)
    private String remark;

    @Column(length = 225)
    private String bizTerm;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_top_level_abie_id", nullable = false)
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

    public BasicBusinessInformationEntity getBbie() {
        return bbie;
    }

    public void setBbie(BasicBusinessInformationEntity bbie) {
        this.bbie = bbie;
    }

    public DataTypeSupplementaryComponent getDtSc() {
        return dtSc;
    }

    public void setDtSc(DataTypeSupplementaryComponent dtSc) {
        this.dtSc = dtSc;
    }

    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction getDtScPriRestri() {
        return dtScPriRestri;
    }

    public void setDtScPriRestri(BusinessDataTypeSupplementaryComponentPrimitiveRestriction dtScPriRestri) {
        this.dtScPriRestri = dtScPriRestri;
    }

    public CodeList getCodeList() {
        return codeList;
    }

    public void setCodeList(CodeList codeList) {
        this.codeList = codeList;
    }

    public long getAgencyIdListId() {
        return (agencyIdListId == null) ? 0L : agencyIdListId;
    }

    public void setAgencyIdListId(long agencyIdListId) {
        this.agencyIdListId = agencyIdListId;
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

    public TopLevelAbie getOwnerTopLevelAbie() {
        return ownerTopLevelAbie;
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
        result = 31 * result + (bbie != null ? bbie.hashCode() : 0);
        result = 31 * result + (dtSc != null ? dtSc.hashCode() : 0);
        result = 31 * result + (dtScPriRestri != null ? dtScPriRestri.hashCode() : 0);
        result = 31 * result + (codeList != null ? codeList.hashCode() : 0);
        result = 31 * result + (agencyIdListId != null ? agencyIdListId.hashCode() : 0);
        result = 31 * result + cardinalityMin;
        result = 31 * result + cardinalityMax;
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (fixedValue != null ? fixedValue.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (bizTerm != null ? bizTerm.hashCode() : 0);
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + (ownerTopLevelAbie != null ? ownerTopLevelAbie.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BasicBusinessInformationEntitySupplementaryComponent{" +
                "bbieScId=" + bbieScId +
                ", guid='" + guid + '\'' +
                ", bbie=" + bbie +
                ", dtSc=" + dtSc +
                ", dtScPriRestri=" + dtScPriRestri +
                ", codeList=" + codeList +
                ", agencyIdListId=" + agencyIdListId +
                ", cardinalityMin=" + cardinalityMin +
                ", cardinalityMax=" + cardinalityMax +
                ", defaultValue='" + defaultValue + '\'' +
                ", fixedValue='" + fixedValue + '\'' +
                ", definition='" + definition + '\'' +
                ", remark='" + remark + '\'' +
                ", bizTerm='" + bizTerm + '\'' +
                ", used=" + used +
                ", ownerTopLevelAbie=" + ownerTopLevelAbie +
                '}';
    }

    @Transient
    private transient List<PersistEventListener> persistEventListeners;

    @Transient
    private transient List<UpdateEventListener> updateEventListeners;

    public BasicBusinessInformationEntitySupplementaryComponent() {
        TimestampAwareEventListener timestampAwareEventListener = new TimestampAwareEventListener();
        addPersistEventListener(timestampAwareEventListener);
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
}
