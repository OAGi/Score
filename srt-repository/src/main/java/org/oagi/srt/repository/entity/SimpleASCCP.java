package org.oagi.srt.repository.entity;

import org.oagi.srt.repository.entity.converter.CoreComponentStateConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class SimpleASCCP implements Serializable {

    @Id
    private long asccpId;

    @Column
    private String guid;

    @Column
    private String propertyTerm;

    @Column
    private String module;

    @Column
    private String definition;

    @Column
    @Convert(attributeName = "state", converter = CoreComponentStateConverter.class)
    private CoreComponentState state;

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

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
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

        SimpleASCCP that = (SimpleASCCP) o;

        if (asccpId != that.asccpId) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (propertyTerm != null ? !propertyTerm.equals(that.propertyTerm) : that.propertyTerm != null) return false;
        if (module != null ? !module.equals(that.module) : that.module != null) return false;
        if (definition != null ? !definition.equals(that.definition) : that.definition != null) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = (int) (asccpId ^ (asccpId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SimpleASCCP{" +
                "asccpId=" + asccpId +
                ", guid='" + guid + '\'' +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", module='" + module + '\'' +
                ", definition='" + definition + '\'' +
                ", state=" + state +
                '}';
    }
}
