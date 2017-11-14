package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.util.Date;

@SqlResultSetMapping(
        name = "top_level_concept",
        entities = {
                @EntityResult(
                        entityClass = AssociationCoreComponentProperty.class,
                        fields = {
                                @FieldResult(name = "asccpId", column = "asccp_id"),
                                @FieldResult(name = "guid", column = "guid"),
                                @FieldResult(name = "releaseId", column = "release_id"),
                                @FieldResult(name = "propertyTerm", column = "property_term"),
                                @FieldResult(name = "lastUpdateTimestamp", column = "last_update_timestamp"),
                        }
                ),
                @EntityResult(
                        entityClass = Module.class,
                        fields = {
                                @FieldResult(name = "module", column = "module"),
                        }
                )
        }
)
@Entity
public class TopLevelConcept {

    @Id
    private long asccpId;

    @Column(length = 41, nullable = false)
    private String guid;

    @Column
    private Long releaseId;

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

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Long getReleaseId() {
        return releaseId;
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
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (releaseId != null ? releaseId.hashCode() : 0);
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TopLevelConcept{" +
                "asccpId=" + asccpId +
                ", guid='" + guid + '\'' +
                ", releaseId=" + releaseId +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", module='" + module + '\'' +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                '}';
    }
}
