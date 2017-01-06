package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.repository.entity.converter.AggregateBusinessInformationEntityStateConverter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "top_level_abie")
public class TopLevelAbie implements Serializable {

    public static final String SEQUENCE_NAME = "TOP_LEVEL_ABIE_ID_SEQ";

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
    private long topLevelAbieId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "abie_id")
    private AggregateBusinessInformationEntity abie;

    @Column(nullable = false)
    @Convert(attributeName = "state", converter = AggregateBusinessInformationEntityStateConverter.class)
    private AggregateBusinessInformationEntityState state;

    @Column(nullable = false)
    private long ownerUserId;

    public long getTopLevelAbieId() {
        return topLevelAbieId;
    }

    public void setTopLevelAbieId(long topLevelAbieId) {
        this.topLevelAbieId = topLevelAbieId;
    }

    public AggregateBusinessInformationEntity getAbie() {
        return abie;
    }

    public void setAbie(AggregateBusinessInformationEntity abie) {
        this.abie = abie;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopLevelAbie that = (TopLevelAbie) o;

        if (topLevelAbieId != 0L && topLevelAbieId == that.topLevelAbieId) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (topLevelAbieId ^ (topLevelAbieId >>> 32));
        result = 31 * result + (abie != null ? (int) (abie.getAbieId() ^ (abie.getAbieId() >>> 32)) : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (int) (ownerUserId ^ (ownerUserId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "TopLevelAbie{" +
                "topLevelAbieId=" + topLevelAbieId +
                ", abie=" + abie +
                ", state=" + state +
                ", ownerUserId=" + ownerUserId +
                '}';
    }
}
