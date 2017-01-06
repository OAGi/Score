package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.repository.entity.converter.CoreComponentStateConverter;

import javax.persistence.*;

@Entity
@Table(name = "bccp")
public class BasicCoreComponentPropertyForLookup {

    public static final String SEQUENCE_NAME = "BCCP_ID_SEQ";

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
    private long bccpId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false, length = 60)
    private String propertyTerm;

    @Column(nullable = false)
    private long bdtId;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(name = "is_deprecated", nullable = false)
    private boolean deprecated;

    @Column(nullable = false)
    @Convert(attributeName = "state", converter = CoreComponentStateConverter.class)
    private CoreComponentState state;

    public long getBccpId() {
        return bccpId;
    }

    public void setBccpId(long bccpId) {
        this.bccpId = bccpId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public long getBdtId() {
        return bdtId;
    }

    public void setBdtId(long bdtId) {
        this.bdtId = bdtId;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public CoreComponentState getState() {
        return state;
    }

    public void setState(CoreComponentState state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicCoreComponentPropertyForLookup that = (BasicCoreComponentPropertyForLookup) o;

        if (bccpId != that.bccpId) return false;
        if (bdtId != that.bdtId) return false;
        if (deprecated != that.deprecated) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (propertyTerm != null ? !propertyTerm.equals(that.propertyTerm) : that.propertyTerm != null) return false;
        if (definition != null ? !definition.equals(that.definition) : that.definition != null) return false;
        return state == that.state;

    }

    @Override
    public int hashCode() {
        int result = (int) (bccpId ^ (bccpId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (int) (bdtId ^ (bdtId >>> 32));
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (deprecated ? 1 : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BasicCoreComponentPropertyForLookup{" +
                "bccpId=" + bccpId +
                ", guid='" + guid + '\'' +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", bdtId=" + bdtId +
                ", definition='" + definition + '\'' +
                ", deprecated=" + deprecated +
                ", state=" + state +
                '}';
    }
}
