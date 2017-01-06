package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "agency_id_list_value")
public class AgencyIdListValue implements Serializable {

    public static final String SEQUENCE_NAME = "AGENCY_ID_LIST_VALUE_ID_SEQ";

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
    private long agencyIdListValueId;

    @Column(nullable = false, length = 150)
    private String value;

    @Column(length = 150)
    private String name;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(nullable = false)
    private long ownerListId;

    public long getAgencyIdListValueId() {
        return agencyIdListValueId;
    }

    public void setAgencyIdListValueId(long agencyIdListValueId) {
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

    public long getOwnerListId() {
        return ownerListId;
    }

    public void setOwnerListId(long ownerListId) {
        this.ownerListId = ownerListId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgencyIdListValue that = (AgencyIdListValue) o;

        if (agencyIdListValueId != 0L && agencyIdListValueId == that.agencyIdListValueId) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (agencyIdListValueId ^ (agencyIdListValueId >>> 32));
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (int) (ownerListId ^ (ownerListId >>> 32));
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
