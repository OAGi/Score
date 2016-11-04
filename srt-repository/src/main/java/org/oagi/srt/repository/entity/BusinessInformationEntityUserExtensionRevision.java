package org.oagi.srt.repository.entity;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(name = "bie_user_ext_revision")
@org.hibernate.annotations.Cache(region = "", usage = CacheConcurrencyStrategy.READ_WRITE)
public class BusinessInformationEntityUserExtensionRevision {

    public static final String SEQUENCE_NAME = "BIE_USER_EXT_REVISION_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long bieUserExtRevisionId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "top_level_abie_id", nullable = false)
    private TopLevelAbie topLevelAbie;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ext_abie_id")
    private AggregateBusinessInformationEntity extAbie;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ext_acc_id", nullable = false)
    private AggregateCoreComponent extAcc;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_ext_acc_id", nullable = false)
    private AggregateCoreComponent userExtAcc;

    @Column(nullable = false)
    private boolean revisedIndicator;

    public long getBieUserExtRevisionId() {
        return bieUserExtRevisionId;
    }

    public void setBieUserExtRevisionId(long bieUserExtRevisionId) {
        this.bieUserExtRevisionId = bieUserExtRevisionId;
    }

    public TopLevelAbie getTopLevelAbie() {
        return topLevelAbie;
    }

    public void setTopLevelAbie(TopLevelAbie topLevelAbie) {
        this.topLevelAbie = topLevelAbie;
    }

    public AggregateBusinessInformationEntity getExtAbie() {
        return extAbie;
    }

    public void setExtAbie(AggregateBusinessInformationEntity extAbie) {
        this.extAbie = extAbie;
    }

    public AggregateCoreComponent getExtAcc() {
        return extAcc;
    }

    public void setExtAcc(AggregateCoreComponent extAcc) {
        this.extAcc = extAcc;
    }

    public AggregateCoreComponent getUserExtAcc() {
        return userExtAcc;
    }

    public void setUserExtAcc(AggregateCoreComponent userExtAcc) {
        this.userExtAcc = userExtAcc;
    }

    public boolean isRevisedIndicator() {
        return revisedIndicator;
    }

    public void setRevisedIndicator(boolean revisedIndicator) {
        this.revisedIndicator = revisedIndicator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BusinessInformationEntityUserExtensionRevision that = (BusinessInformationEntityUserExtensionRevision) o;

        if (bieUserExtRevisionId != that.bieUserExtRevisionId) return false;
        if (revisedIndicator != that.revisedIndicator) return false;
        if (topLevelAbie != null ? !topLevelAbie.equals(that.topLevelAbie) : that.topLevelAbie != null) return false;
        if (extAbie != null ? !extAbie.equals(that.extAbie) : that.extAbie != null) return false;
        if (extAcc != null ? !extAcc.equals(that.extAcc) : that.extAcc != null) return false;
        return userExtAcc != null ? userExtAcc.equals(that.userExtAcc) : that.userExtAcc == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (bieUserExtRevisionId ^ (bieUserExtRevisionId >>> 32));
        result = 31 * result + (topLevelAbie != null ? topLevelAbie.hashCode() : 0);
        result = 31 * result + (extAbie != null ? extAbie.hashCode() : 0);
        result = 31 * result + (extAcc != null ? extAcc.hashCode() : 0);
        result = 31 * result + (userExtAcc != null ? userExtAcc.hashCode() : 0);
        result = 31 * result + (revisedIndicator ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BusinessInformationEntityUserExtensionRevision{" +
                "bieUserExtRevisionId=" + bieUserExtRevisionId +
                ", topLevelAbie=" + topLevelAbie +
                ", extAbie=" + extAbie +
                ", extAcc=" + extAcc +
                ", userExtAcc=" + userExtAcc +
                ", revisedIndicator=" + revisedIndicator +
                '}';
    }
}
