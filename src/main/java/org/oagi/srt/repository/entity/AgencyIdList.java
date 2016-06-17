package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "agency_id_list")
public class AgencyIdList implements Serializable {

    public static final String SEQUENCE_NAME = "AGENCY_ID_LIST_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
            }
    )
    private int agencyIdListId;

    @Column(nullable = false)
    private String guid;

    @Column(nullable = false)
    private String enumTypeGuid;

    @Column
    private String name;

    @Column
    private String listId;

    @Column
    private Integer agencyId = null;

    @Column
    private String versionId;

    @Lob
    @Column
    private String definition;

    public int getAgencyIdListId() {
        return agencyIdListId;
    }

    public void setAgencyIdListId(int agencyIdListId) {
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

    public int getAgencyId() {
        return (agencyId == null) ? 0 : agencyId;
    }

    public void setAgencyId(int agencyId) {
        this.agencyId = agencyId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
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
        if (agencyId != null ? !agencyId.equals(that.agencyId) : that.agencyId != null) return false;
        if (versionId != null ? !versionId.equals(that.versionId) : that.versionId != null) return false;
        return definition != null ? definition.equals(that.definition) : that.definition == null;

    }

    @Override
    public int hashCode() {
        int result = agencyIdListId;
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (enumTypeGuid != null ? enumTypeGuid.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (listId != null ? listId.hashCode() : 0);
        result = 31 * result + (agencyId != null ? agencyId.hashCode() : 0);
        result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
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
                ", agencyId=" + agencyId +
                ", versionId='" + versionId + '\'' +
                ", definition='" + definition + '\'' +
                '}';
    }
}
