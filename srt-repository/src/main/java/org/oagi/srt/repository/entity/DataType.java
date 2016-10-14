package org.oagi.srt.repository.entity;

import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "dt")
public class DataType implements Serializable {

    public static final String SEQUENCE_NAME = "DT_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long dtId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false)
    private int type;

    @Column(nullable = false, length = 45)
    private String versionNum;

    @Column
    private Long previousVersionDtId;

    @Column(length = 45)
    private String dataTypeTerm;

    @Column(length = 100)
    private String qualifier;

    @Column
    private Long basedDtId;

    @Column(nullable = false, length = 200)
    private String den;

    @Column(length = 200)
    private String contentComponentDen;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Lob
    @Column(length = 10 * 1024)
    private String contentComponentDefinition;

    @Lob
    @Column(length = 10 * 1024)
    private String revisionDoc;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private Module module;

    @Column
    private int state;

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
    private int revisionNum;

    @Column(nullable = false)
    private int revisionTrackingNum;

    @Column
    private Integer revisionAction;

    @Column
    private Long releaseId;

    @Column
    private Long currentBdtId;

    @Column(name = "is_deprecated", nullable = false)
    private boolean deprecated;

    public DataType() {
    }

    public DataType(long dtId, String dataTypeTerm) {
        this.dtId = dtId;
        this.dataTypeTerm = dataTypeTerm;
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

    public long getDtId() {
        return dtId;
    }

    public void setDtId(long dtId) {
        this.dtId = dtId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

    public long getPreviousVersionDtId() {
        return (previousVersionDtId == null) ? 0L : previousVersionDtId;
    }

    public void setPreviousVersionDtId(long previousVersionDtId) {
        this.previousVersionDtId = previousVersionDtId;
    }

    public String getDataTypeTerm() {
        return dataTypeTerm;
    }

    public void setDataTypeTerm(String dataTypeTerm) {
        this.dataTypeTerm = dataTypeTerm;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public long getBasedDtId() {
        return (basedDtId == null) ? 0L : basedDtId;
    }

    public void setBasedDtId(long basedDtId) {
        this.basedDtId = basedDtId;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public String getContentComponentDen() {
        return contentComponentDen;
    }

    public void setContentComponentDen(String contentComponentDen) {
        this.contentComponentDen = contentComponentDen;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getContentComponentDefinition() {
        return contentComponentDefinition;
    }

    public void setContentComponentDefinition(String contentComponentDefinition) {
        this.contentComponentDefinition = contentComponentDefinition;
    }

    public String getRevisionDoc() {
        return revisionDoc;
    }

    public void setRevisionDoc(String revisionDoc) {
        if (!StringUtils.isEmpty(revisionDoc)) {
            this.revisionDoc = revisionDoc;
        }
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
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

    public int getRevisionAction() {
        return (revisionAction == null) ? 0 : revisionAction;
    }

    public void setRevisionAction(int revisionAction) {
        this.revisionAction = revisionAction;
    }

    public long getReleaseId() {
        return (releaseId == null) ? 0L : releaseId;
    }

    public void setReleaseId(long releaseId) {
        this.releaseId = releaseId;
    }

    public long getCurrentBdtId() {
        return (currentBdtId == null) ? 0L : currentBdtId;
    }

    public void setCurrentBdtId(long currentBdtId) {
        this.currentBdtId = currentBdtId;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataType that = (DataType) o;

        if (dtId != 0L && dtId == that.dtId) return true;
        if (guid != null) {
            if (guid.equals(that.guid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (dtId ^ (dtId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + type;
        result = 31 * result + (versionNum != null ? versionNum.hashCode() : 0);
        result = 31 * result + (previousVersionDtId != null ? previousVersionDtId.hashCode() : 0);
        result = 31 * result + (dataTypeTerm != null ? dataTypeTerm.hashCode() : 0);
        result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
        result = 31 * result + (basedDtId != null ? basedDtId.hashCode() : 0);
        result = 31 * result + (den != null ? den.hashCode() : 0);
        result = 31 * result + (contentComponentDen != null ? contentComponentDen.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (contentComponentDefinition != null ? contentComponentDefinition.hashCode() : 0);
        result = 31 * result + (revisionDoc != null ? revisionDoc.hashCode() : 0);
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + state;
        result = 31 * result + (int) (createdBy ^ (createdBy >>> 32));
        result = 31 * result + (int) (ownerUserId ^ (ownerUserId >>> 32));
        result = 31 * result + (int) (lastUpdatedBy ^ (lastUpdatedBy >>> 32));
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + revisionNum;
        result = 31 * result + revisionTrackingNum;
        result = 31 * result + (revisionAction != null ? revisionAction.hashCode() : 0);
        result = 31 * result + (releaseId != null ? releaseId.hashCode() : 0);
        result = 31 * result + (currentBdtId != null ? currentBdtId.hashCode() : 0);
        result = 31 * result + (deprecated ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DataType{" +
                "dtId=" + dtId +
                ", guid='" + guid + '\'' +
                ", type=" + type +
                ", versionNum='" + versionNum + '\'' +
                ", previousVersionDtId=" + previousVersionDtId +
                ", dataTypeTerm='" + dataTypeTerm + '\'' +
                ", qualifier='" + qualifier + '\'' +
                ", basedDtId=" + basedDtId +
                ", den='" + den + '\'' +
                ", contentComponentDen='" + contentComponentDen + '\'' +
                ", definition='" + definition + '\'' +
                ", contentComponentDefinition='" + contentComponentDefinition + '\'' +
                ", revisionDoc='" + revisionDoc + '\'' +
                ", module=" + module +
                ", state=" + state +
                ", createdBy=" + createdBy +
                ", ownerUserId=" + ownerUserId +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", revisionNum=" + revisionNum +
                ", revisionTrackingNum=" + revisionTrackingNum +
                ", revisionAction=" + revisionAction +
                ", releaseId=" + releaseId +
                ", currentBdtId=" + currentBdtId +
                ", deprecated=" + deprecated +
                '}';
    }
}
