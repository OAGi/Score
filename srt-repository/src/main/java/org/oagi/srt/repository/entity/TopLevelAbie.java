package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "top_level_abie")
public class TopLevelAbie implements Serializable {

    public static final String SEQUENCE_NAME = "TOP_LEVEL_ABIE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long topLevelAbieId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "abie_id")
    private AggregateBusinessInformationEntity abie;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopLevelAbie that = (TopLevelAbie) o;

        if (topLevelAbieId != that.topLevelAbieId) return false;
        return abie != null ? abie.equals(that.abie) : that.abie == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (topLevelAbieId ^ (topLevelAbieId >>> 32));
        result = 31 * result + (abie != null ? abie.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TopLevelAbie{" +
                "topLevelAbieId=" + topLevelAbieId +
                ", abie=" + abie +
                '}';
    }
}
