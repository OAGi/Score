package org.oagi.srt.repository.entity;

import org.oagi.srt.repository.entity.converter.AggregateBusinessInformationEntityStateConverter;

import javax.persistence.*;
import java.util.Date;

@SqlResultSetMapping(
        name="profile_bod",
        entities = {
                @EntityResult(
                        entityClass = TopLevelAbie.class,
                        fields = {
                                @FieldResult(name = "topLevelAbieId", column = "top_level_abie_id"),
                                @FieldResult(name = "state", column = "state"),
                                @FieldResult(name = "ownerUserId", column = "owner_user_id"),
                        }
                ),
                @EntityResult(
                        entityClass = AggregateBusinessInformationEntity.class,
                        fields = {
                                @FieldResult(name = "abieId", column = "abie_id"),
                                @FieldResult(name = "creationTimestamp", column = "creation_timestamp"),
                                @FieldResult(name = "lastUpdateTimestamp", column = "last_update_timestamp"),
                                @FieldResult(name = "version", column = "version"),
                                @FieldResult(name = "status", column = "status"),
                        }
                ),
                @EntityResult(
                        entityClass = AssociationBusinessInformationEntityProperty.class,
                        fields = {
                                @FieldResult(name = "asbiepId", column = "asbiep_id"),
                        }
                ),
                @EntityResult(
                        entityClass = AssociationCoreComponentProperty.class,
                        fields = {
                                @FieldResult(name = "asccpId", column = "asccp_id"),
                                @FieldResult(name = "propertyTerm", column = "property_term"),
                                @FieldResult(name = "releaseId", column = "release_id"),
                        }
                ),
                @EntityResult(
                        entityClass = BusinessContext.class,
                        fields = {
                                @FieldResult(name = "bizCtxId", column = "biz_ctx_id"),
                                @FieldResult(name = "bizCtxName", column = "biz_ctx_name"),
                        }
                ),
                @EntityResult(
                        entityClass = User.class,
                        fields = {
                                @FieldResult(name = "ownerName", column = "owner_name"),
                        }
                ),
                @EntityResult(
                        entityClass = Release.class,
                        fields = {
                                @FieldResult(name = "releaseNum", column = "releaseNum"),
                        }
                )
        }
)
@Entity
public class ProfileBIE {

    @Id
    private long topLevelAbieId;

    @Column(nullable = false)
    private long abieId;

    @Column(length = 45)
    private String version;

    @Column(length = 45)
    private String status;

    @Column(nullable = false)
    private long asbiepId;

    @Column(nullable = false)
    private long asccpId;

    @Column
    private Long releaseId;

    @Column(nullable = false)
    private String propertyTerm;

    @Column(nullable = false)
    private long bizCtxId;

    @Column(length = 100)
    private String bizCtxName;

    @Column(length = 45)
    private String releaseNum;

    @Column
    @Convert(attributeName = "state", converter = AggregateBusinessInformationEntityStateConverter.class)
    private AggregateBusinessInformationEntityState state;

    @Column(nullable = false, updatable = false)
    private long ownerUserId;

    @Column(length = 100)
    private String ownerName;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

    public ProfileBIE() {
    }

    public ProfileBIE(long topLevelAbieId, long abieId, String version, String status, long asbiepId, long asccpId, Long releaseId,
                      String propertyTerm, long bizCtxId, String bizCtxName, String releaseNum,
                      AggregateBusinessInformationEntityState state, long ownerUserId, String ownerName, Date creationTimestamp, Date lastUpdateTimestamp) {
        this.topLevelAbieId = topLevelAbieId;
        this.abieId = abieId;
        this.version = version;
        this.status = status;
        this.asbiepId = asbiepId;
        this.asccpId = asccpId;
        this.releaseId = releaseId;
        this.propertyTerm = propertyTerm;
        this.bizCtxId = bizCtxId;
        this.bizCtxName = bizCtxName;
        this.releaseNum = releaseNum;
        this.state = state;
        this.ownerUserId = ownerUserId;
        this.ownerName = ownerName;
        this.creationTimestamp = creationTimestamp;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public long getTopLevelAbieId() {
        return topLevelAbieId;
    }

    public void setTopLevelAbieId(long topLevelAbieId) {
        this.topLevelAbieId = topLevelAbieId;
    }

    public long getAbieId() {
        return abieId;
    }

    public void setAbieId(long abieId) {
        this.abieId = abieId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getAsbiepId() {
        return asbiepId;
    }

    public void setAsbiepId(long asbiepId) {
        this.asbiepId = asbiepId;
    }

    public long getAsccpId() {
        return asccpId;
    }

    public void setAsccpId(long asccpId) {
        this.asccpId = asccpId;
    }

    public long getReleaseId() {
        return (releaseId != null) ? releaseId : 0L;
    }

    public void setReleaseId(Long releaseId) {
        this.releaseId = releaseId;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public long getBizCtxId() {
        return bizCtxId;
    }

    public void setBizCtxId(long bizCtxId) {
        this.bizCtxId = bizCtxId;
    }

    public String getBizCtxName() {
        return bizCtxName;
    }

    public void setBizCtxName(String bizCtxName) {
        this.bizCtxName = bizCtxName;
    }

    public String getReleaseNum() {
        return releaseNum;
    }

    public void setReleaseNum(String releaseNum) {
        this.releaseNum = releaseNum;
    }

    public AggregateBusinessInformationEntityState getState() {
        return state;
    }

    public void setState(AggregateBusinessInformationEntityState state) {
        this.state = state;
    }

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileBIE that = (ProfileBIE) o;

        if (topLevelAbieId != 0L && topLevelAbieId == that.topLevelAbieId) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (topLevelAbieId ^ (topLevelAbieId >>> 32));
        result = 31 * result + (int) (abieId ^ (abieId >>> 32));
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (int) (asbiepId ^ (asbiepId >>> 32));
        result = 31 * result + (int) (asccpId ^ (asccpId >>> 32));
        result = 31 * result + (releaseId != null ? releaseId.hashCode() : 0);
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (int) (bizCtxId ^ (bizCtxId >>> 32));
        result = 31 * result + (bizCtxName != null ? bizCtxName.hashCode() : 0);
        result = 31 * result + (releaseNum != null ? releaseNum.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (int) (ownerUserId ^ (ownerUserId >>> 32));
        result = 31 * result + (ownerName != null ? ownerName.hashCode() : 0);
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProfileBIE{" +
                "topLevelAbieId=" + topLevelAbieId +
                ", abieId=" + abieId +
                ", version='" + version + '\'' +
                ", status='" + status + '\'' +
                ", asbiepId=" + asbiepId +
                ", asccpId=" + asccpId +
                ", releaseId=" + releaseId +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", bizCtxId=" + bizCtxId +
                ", bizCtxName='" + bizCtxName + '\'' +
                ", releaseNum='" + releaseNum + '\'' +
                ", state=" + state +
                ", ownerUserId=" + ownerUserId +
                ", ownerName='" + ownerName + '\'' +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                '}';
    }
}
