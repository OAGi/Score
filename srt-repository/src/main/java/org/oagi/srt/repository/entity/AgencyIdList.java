package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "agency_id_list")
public class AgencyIdList implements Serializable {

    public static final String SEQUENCE_NAME = "AGENCY_ID_LIST_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long agencyIdListId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false, length = 41)
    private String enumTypeGuid;

    @Column(length = 100)
    private String name;

    @Column(length = 10)
    private String listId;

    @Column
    private Long agencyIdListValueId = null;

    @Column(length = 10)
    private String versionId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id")
    private Module module;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    public long getAgencyIdListId() {
        return agencyIdListId;
    }

    public void setAgencyIdListId(long agencyIdListId) {
        this.agencyIdListId = agencyIdListId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getEnumTypeGuid() {
        return enumTypeGuid;
    }

    public void setEnumTypeGuid(String enumTypeGuid) {
        this.enumTypeGuid = enumTypeGuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public long getAgencyIdListValueId() {
        return (agencyIdListValueId == null) ? 0L : agencyIdListValueId;
    }

    public void setAgencyIdListValueId(long agencyIdListValueId) {
        this.agencyIdListValueId = agencyIdListValueId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgencyIdList that = (AgencyIdList) o;

        if (agencyIdListId != that.agencyIdListId) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (enumTypeGuid != null ? !enumTypeGuid.equals(that.enumTypeGuid) : that.enumTypeGuid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (listId != null ? !listId.equals(that.listId) : that.listId != null) return false;
        if (agencyIdListValueId != null ? !agencyIdListValueId.equals(that.agencyIdListValueId) : that.agencyIdListValueId != null)
            return false;
        if (versionId != null ? !versionId.equals(that.versionId) : that.versionId != null) return false;
        if (module != null ? !module.equals(that.module) : that.module != null) return false;
        return definition != null ? definition.equals(that.definition) : that.definition == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (agencyIdListId ^ (agencyIdListId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (enumTypeGuid != null ? enumTypeGuid.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (listId != null ? listId.hashCode() : 0);
        result = 31 * result + (agencyIdListValueId != null ? agencyIdListValueId.hashCode() : 0);
        result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AgencyIdList{" +
                "agencyIdListId=" + agencyIdListId +
                ", guid='" + guid + '\'' +
                ", enumTypeGuid='" + enumTypeGuid + '\'' +
                ", name='" + name + '\'' +
                ", listId='" + listId + '\'' +
                ", agencyIdListValueId=" + agencyIdListValueId +
                ", versionId='" + versionId + '\'' +
                ", module=" + module +
                ", definition='" + definition + '\'' +
                '}';
    }
}
