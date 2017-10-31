package org.oagi.srt.repository.entity;

import org.oagi.srt.repository.entity.converter.CoreComponentStateConverter;
import org.oagi.srt.repository.entity.converter.OagisComponentTypeConverter;

import javax.persistence.*;
import java.util.Date;

@Entity
public class CoreComponents {

    @EmbeddedId
    private CoreComponentsId coreComponentsId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false, length = 200)
    private String den;

    @Column(nullable = false)
    private long ownerUserId;

    @Column(nullable = false, length = 45)
    private String owner;

    @Column(nullable = false)
    @Convert(attributeName = "state", converter = CoreComponentStateConverter.class)
    private CoreComponentState state;

    @Column
    @Convert(attributeName = "oagisComponentType", converter = OagisComponentTypeConverter.class)
    private OagisComponentType oagisComponentType;

    @Column(nullable = false, length = 45)
    private String lastUpdatedUser;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

    @Column(length = 100, nullable = false)
    private String module;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    public CoreComponentsId getCoreComponentsId() {
        return coreComponentsId;
    }

    public void setCoreComponentsId(CoreComponentsId coreComponentsId) {
        this.coreComponentsId = coreComponentsId;
    }

    public long getId() {
        return coreComponentsId.getId();
    }

    public void setId(long id) {
        coreComponentsId.setId(id);
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getType() {
        return coreComponentsId.getType();
    }

    public void setType(String type) {
        coreComponentsId.setType(type);
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public CoreComponentState getState() {
        return state;
    }

    public void setState(CoreComponentState state) {
        this.state = state;
    }

    public OagisComponentType getOagisComponentType() {
        return oagisComponentType;
    }

    public void setOagisComponentType(OagisComponentType oagisComponentType) {
        this.oagisComponentType = oagisComponentType;
    }

    public String getLastUpdatedUser() {
        return lastUpdatedUser;
    }

    public void setLastUpdatedUser(String lastUpdatedUser) {
        this.lastUpdatedUser = lastUpdatedUser;
    }

    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoreComponents that = (CoreComponents) o;

        if (ownerUserId != that.ownerUserId) return false;
        if (coreComponentsId != null ? !coreComponentsId.equals(that.coreComponentsId) : that.coreComponentsId != null)
            return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (den != null ? !den.equals(that.den) : that.den != null) return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
        if (state != that.state) return false;
        if (oagisComponentType != that.oagisComponentType) return false;
        if (lastUpdatedUser != null ? !lastUpdatedUser.equals(that.lastUpdatedUser) : that.lastUpdatedUser != null)
            return false;
        if (lastUpdateTimestamp != null ? !lastUpdateTimestamp.equals(that.lastUpdateTimestamp) : that.lastUpdateTimestamp != null)
            return false;
        if (module != null ? !module.equals(that.module) : that.module != null) return false;
        return definition != null ? definition.equals(that.definition) : that.definition == null;
    }

    @Override
    public int hashCode() {
        int result = coreComponentsId != null ? coreComponentsId.hashCode() : 0;
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (den != null ? den.hashCode() : 0);
        result = 31 * result + (int) (ownerUserId ^ (ownerUserId >>> 32));
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (oagisComponentType != null ? oagisComponentType.hashCode() : 0);
        result = 31 * result + (lastUpdatedUser != null ? lastUpdatedUser.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CoreComponents{" +
                "coreComponentsId=" + coreComponentsId +
                ", guid='" + guid + '\'' +
                ", den='" + den + '\'' +
                ", ownerUserId=" + ownerUserId +
                ", owner='" + owner + '\'' +
                ", state=" + state +
                ", oagisComponentType=" + oagisComponentType +
                ", lastUpdatedUser='" + lastUpdatedUser + '\'' +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", module='" + module + '\'' +
                ", definition='" + definition + '\'' +
                '}';
    }
}
