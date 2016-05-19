package org.oagi.srt.repository.entity;

import java.io.Serializable;

public class AgencyIdListValue implements Serializable {

    private int agencyIdListValueId;
    private String value;
    private String name;
    private String definition;
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
