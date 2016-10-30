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
                        }
                ),
                @EntityResult(
                        entityClass = AggregateBusinessInformationEntity.class,
                        fields = {
                                @FieldResult(name = "abieId", column = "abie_id"),
                                @FieldResult(name = "state", column = "state"),
                                @FieldResult(name = "owner", column = "owner"),
                                @FieldResult(name = "creationTimestamp", column = "creation_timestamp"),
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
                )
        }
)
@Entity
public class ProfileBOD {

    @Id
    private long topLevelAbieId;

    @Column(nullable = false)
    private long abieId;

    @Column(nullable = false)
    private long asbiepId;

    @Column(nullable = false)
    private long asccpId;

    @Column(nullable = false)
    private String propertyTerm;

    @Column(nullable = false)
    private long bizCtxId;

    @Column(length = 100)
    private String bizCtxName;

    @Column
    @Convert(attributeName = "state", converter = AggregateBusinessInformationEntityStateConverter.class)
    private AggregateBusinessInformationEntityState state;

    @Column(nullable = false, updatable = false)
    private long owner;

    @Column(length = 100)
    private String ownerName;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

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

    public AggregateBusinessInformationEntityState getState() {
        return state;
    }

    public void setState(AggregateBusinessInformationEntityState state) {
        this.state = state;
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileBOD that = (ProfileBOD) o;

        if (topLevelAbieId != 0L && topLevelAbieId == that.topLevelAbieId) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (topLevelAbieId ^ (topLevelAbieId >>> 32));
        result = 31 * result + (int) (abieId ^ (abieId >>> 32));
        result = 31 * result + (int) (asbiepId ^ (asbiepId >>> 32));
        result = 31 * result + (int) (asccpId ^ (asccpId >>> 32));
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (int) (bizCtxId ^ (bizCtxId >>> 32));
        result = 31 * result + (bizCtxName != null ? bizCtxName.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (int) (owner ^ (owner >>> 32));
        result = 31 * result + (ownerName != null ? ownerName.hashCode() : 0);
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProfileBOD{" +
                "topLevelAbieId=" + topLevelAbieId +
                ", abieId=" + abieId +
                ", asbiepId=" + asbiepId +
                ", asccpId=" + asccpId +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", bizCtxId=" + bizCtxId +
                ", bizCtxName='" + bizCtxName + '\'' +
                ", state=" + state +
                ", owner=" + owner +
                ", ownerName='" + ownerName + '\'' +
                ", creationTimestamp=" + creationTimestamp +
                '}';
    }
}
