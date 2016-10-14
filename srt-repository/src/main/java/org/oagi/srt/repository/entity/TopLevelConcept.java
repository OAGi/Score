package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "v_top_level_concept")
public class TopLevelConcept {

    @Id
    private long asccpId;

    @Column(nullable = false)
    private String propertyTerm;

    @Column(length = 100, nullable = false)
    private String module;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
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

        TopLevelConcept that = (TopLevelConcept) o;

        if (asccpId != 0L && asccpId == that.asccpId) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (asccpId ^ (asccpId >>> 32));
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TopLevelConcept{" +
                "asccpId=" + asccpId +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", module='" + module + '\'' +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                '}';
    }
}
