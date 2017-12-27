package org.oagi.srt.repository.entity;

import org.oagi.srt.repository.entity.converter.CoreComponentStateConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class SimpleBCCP implements Serializable {

    @Id
    private long bccpId;

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

    @Column
    private Long ownerUserId;

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

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleBCCP that = (SimpleBCCP) o;
        return bccpId == that.bccpId &&
                Objects.equals(guid, that.guid) &&
                Objects.equals(propertyTerm, that.propertyTerm) &&
                Objects.equals(module, that.module) &&
                Objects.equals(definition, that.definition) &&
                state == that.state &&
                Objects.equals(ownerUserId, that.ownerUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bccpId, guid, propertyTerm, module, definition, state, ownerUserId);
    }

    @Override
    public String toString() {
        return "SimpleBCCP{" +
                "bccpId=" + bccpId +
                ", guid='" + guid + '\'' +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", module='" + module + '\'' +
                ", definition='" + definition + '\'' +
                ", state=" + state +
                ", ownerUserId=" + ownerUserId +
                '}';
    }
}
