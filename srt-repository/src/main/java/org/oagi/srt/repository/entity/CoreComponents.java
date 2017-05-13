package org.oagi.srt.repository.entity;

import org.oagi.srt.repository.entity.converter.CoreComponentStateConverter;
import org.oagi.srt.repository.entity.converter.OagisComponentTypeConverter;

import javax.persistence.*;
import java.util.Date;

@Entity
public class CoreComponents {

    @Id
    private long id;

    @Column(nullable = false, length = 5)
    private String type;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
