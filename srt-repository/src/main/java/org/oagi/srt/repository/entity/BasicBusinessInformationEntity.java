package org.oagi.srt.repository.entity;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "bbie")
@org.hibernate.annotations.Cache(region = "", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class BasicBusinessInformationEntity implements Serializable, BusinessInformationEntity, IdEntity, IGuidEntity {

    public static final String SEQUENCE_NAME = "BBIE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long bbieId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false)
    private long basedBccId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_abie_id", nullable = false)
    private AggregateBusinessInformationEntity fromAbie;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_bbiep_id", nullable = false)
    private BasicBusinessInformationEntityProperty toBbiep;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bdt_pri_restri_id")
    private BusinessDataTypePrimitiveRestriction bdtPriRestri;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "code_list_id")
    private CodeList codeList;

    @Column(nullable = false)
    private int cardinalityMin;

    @Column
    private int cardinalityMax;

    @Column
    private String defaultValue;

    @Column(name = "is_nillable", nullable = false)
    private boolean nillable;

    @Column
    private String fixedValue;

    @Column(name = "is_null", nullable = false)
    private boolean nill;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(length = 225)
    private String remark;

    @Column(nullable = false, updatable = false)
    private long createdBy;

    @Column(nullable = false)
    private long lastUpdatedBy;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

    @Column(nullable = false)
    private double seqKey;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_top_level_abie_id", nullable = false)
    private TopLevelAbie ownerTopLevelAbie;

    @PrePersist
    public void prePersist() {
        creationTimestamp = new Date();
        lastUpdateTimestamp = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdateTimestamp = new Date();
    }

    @Override
    public long getId() {
        return getBbieId();
    }

    @Override
    public void setId(long id) {
        setBbieId(id);
    }

    public long getBbieId() {
        return bbieId;
    }

    public void setBbieId(long bbieId) {
        this.bbieId = bbieId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public long getBasedBccId() {
        return basedBccId;
    }

    public void setBasedBccId(long basedBccId) {
        this.basedBccId = basedBccId;
    }

    public AggregateBusinessInformationEntity getFromAbie() {
        return fromAbie;
    }

    public void setFromAbie(AggregateBusinessInformationEntity fromAbie) {
        this.fromAbie = fromAbie;
    }

    public BasicBusinessInformationEntityProperty getToBbiep() {
        return toBbiep;
    }

    public void setToBbiep(BasicBusinessInformationEntityProperty toBbiep) {
        this.toBbiep = toBbiep;
    }

    public BusinessDataTypePrimitiveRestriction getBdtPriRestri() {
        return bdtPriRestri;
    }

    public void setBdtPriRestri(BusinessDataTypePrimitiveRestriction bdtPriRestri) {
        this.bdtPriRestri = bdtPriRestri;
    }

    public CodeList getCodeList() {
        return codeList;
    }

    public void setCodeList(CodeList codeList) {
        this.codeList = codeList;
    }

    public int getCardinalityMin() {
        return cardinalityMin;
    }

    public void setCardinalityMin(int cardinalityMin) {
        if (cardinalityMin < 0) {
            throw new IllegalArgumentException("'cardinalityMin' argument must be 0 or greater: " + cardinalityMin);
        }
        this.cardinalityMin = cardinalityMin;
    }

    public int getCardinalityMax() {
        return cardinalityMax;
    }

    public void setCardinalityMax(int cardinalityMax) {
        if (cardinalityMax < -1) {
            throw new IllegalArgumentException("'cardinalityMax' argument must be -1 or greater: " + cardinalityMax);
        }
        this.cardinalityMax = cardinalityMax;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }

    public boolean isNill() {
        return nill;
    }

    public void setNill(boolean nill) {
        this.nill = nill;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
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

    public double getSeqKey() {
        return seqKey;
    }

    public void setSeqKey(double seqKey) {
        this.seqKey = seqKey;
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

        BasicBusinessInformationEntity that = (BasicBusinessInformationEntity) o;

        if (bbieId != 0L && bbieId == that.bbieId) return true;
        if (guid != null) {
            if (guid.equals(that.guid)) {
                return true;
            }
        }
        return false;
    }
}
