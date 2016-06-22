package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "agency_id_list_value")
public class AgencyIdListValue implements Serializable {

    public static final String SEQUENCE_NAME = "AGENCY_ID_LIST_VALUE_ID_SEQ";

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
    private int agencyIdListValueId;

    @Column(nullable = false, length = 150)
    private String value;

    @Column(length = 150)
    private String name;

    @Lob
    @Column(length = 10 * 1024)
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgencyIdListValue that = (AgencyIdListValue) o;

        if (agencyIdListValueId != that.agencyIdListValueId) return false;
        if (ownerListId != that.ownerListId) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return definition != null ? definition.equals(that.definition) : that.definition == null;

    }

    @Override
    public int hashCode() {
        int result = agencyIdListValueId;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + ownerListId;
        return result;
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
