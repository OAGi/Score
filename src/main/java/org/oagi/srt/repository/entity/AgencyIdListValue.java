package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "agency_id_list_value")
public class AgencyIdListValue implements Serializable {

    @Id
    @GeneratedValue(generator = "AGENCY_ID_LIST_VALUE_ID_SEQ", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "AGENCY_ID_LIST_VALUE_ID_SEQ", sequenceName = "AGENCY_ID_LIST_VALUE_ID_SEQ", allocationSize = 1)
    private int agencyIdListValueId;

    @Column(nullable = false)
    private String value;

    @Column
    private String name;

    @Column
    private String definition;

    @Column(nullable = false)
    private int ownerListId;

    public int getAgencyIdListValueId() {
        return agencyIdListValueId;
    }

    public void setAgencyIdListValueId(int agencyIdListValueId) {
        this.agencyIdListValueId = agencyIdListValueId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getOwnerListId() {
        return ownerListId;
    }

    public void setOwnerListId(int ownerListId) {
        this.ownerListId = ownerListId;
    }

    @Override
    public String toString() {
        return "AgencyIdListValue{" +
                "agencyIdListValueId=" + agencyIdListValueId +
                ", value='" + value + '\'' +
                ", name='" + name + '\'' +
                ", definition='" + definition + '\'' +
                ", ownerListId=" + ownerListId +
                '}';
    }
}
