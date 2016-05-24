package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "agency_id_list")
public class AgencyIdList implements Serializable {

    @Id
    @GeneratedValue(generator = "AGENCY_ID_LIST_ID_SEQ", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "AGENCY_ID_LIST_ID_SEQ", sequenceName = "AGENCY_ID_LIST_ID_SEQ", allocationSize = 1)
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
