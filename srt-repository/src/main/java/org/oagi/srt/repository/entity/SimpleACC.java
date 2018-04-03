package org.oagi.srt.repository.entity;

import org.oagi.srt.repository.entity.converter.CoreComponentStateConverter;
import org.oagi.srt.repository.entity.converter.OagisComponentTypeConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class SimpleACC implements OwnerUserAware, Serializable {

    @Id
    private long accId;

    @Column
    private String guid;

    @Column
    private String objectClassTerm;

    @Column
    private String module;

    @Column
    private String definition;

    @Column
    @Convert(attributeName = "oagisComponentType", converter = OagisComponentTypeConverter.class)
    private OagisComponentType oagisComponentType;

    @Column
    @Convert(attributeName = "state", converter = CoreComponentStateConverter.class)
    private CoreComponentState state;

    @Column(nullable = false)
    private int revisionNum;

    @Column(nullable = false)
    private int revisionTrackingNum;

    @Column
    private Long releaseId;

    @Column
    private long ownerUserId;

    @Column(name = "is_abstract", nullable = false)
    private boolean isAbstract;

    public long getAccId() {
        return accId;
    }

    public void setAccId(long accId) {
        this.accId = accId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getObjectClassTerm() {
        return objectClassTerm;
    }

    public void setObjectClassTerm(String objectClassTerm) {
        this.objectClassTerm = objectClassTerm;
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

    public OagisComponentType getOagisComponentType() {
        return oagisComponentType;
    }

    public void setOagisComponentType(OagisComponentType oagisComponentType) {
        this.oagisComponentType = oagisComponentType;
    }

    public CoreComponentState getState() {
        return state;
    }

    public void setState(CoreComponentState state) {
        this.state = state;
    }

    public int getRevisionNum() {
        return revisionNum;
    }

    public void setRevisionNum(int revisionNum) {
        this.revisionNum = revisionNum;
    }

    public int getRevisionTrackingNum() {
        return revisionTrackingNum;
    }

    public void setRevisionTrackingNum(int revisionTrackingNum) {
        this.revisionTrackingNum = revisionTrackingNum;
    }

    public String getRevision() {
        return getRevisionNum() + "." + getRevisionTrackingNum();
    }

    public long getReleaseId() {
        return (releaseId == null) ? 0L : releaseId;
    }

    public void setReleaseId(Long releaseId) {
        this.releaseId = releaseId;
    }

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleACC simpleACC = (SimpleACC) o;
        return accId == simpleACC.accId &&
                revisionNum == simpleACC.revisionNum &&
                revisionTrackingNum == simpleACC.revisionTrackingNum &&
                ownerUserId == simpleACC.ownerUserId &&
                isAbstract == simpleACC.isAbstract &&
                Objects.equals(guid, simpleACC.guid) &&
                Objects.equals(objectClassTerm, simpleACC.objectClassTerm) &&
                Objects.equals(module, simpleACC.module) &&
                Objects.equals(definition, simpleACC.definition) &&
                oagisComponentType == simpleACC.oagisComponentType &&
                state == simpleACC.state &&
                Objects.equals(releaseId, simpleACC.releaseId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(accId, guid, objectClassTerm, module, definition, oagisComponentType, state, revisionNum, revisionTrackingNum, releaseId, ownerUserId, isAbstract);
    }
}
