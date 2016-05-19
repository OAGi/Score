package org.oagi.srt.repository.entity;

import org.oagi.srt.common.SRTConstants;

import java.io.Serializable;
import java.util.Date;

public class CodeList implements Serializable {

    private int codeListId;
    private String guid;
    private String enumTypeGuid;
    private String name;
    private String listId;
    private int agencyId;
    private String versionId;
    private String definition;
    private String remark;
    private String definitionSource;
    private int basedCodeListId;
    private boolean extensibleIndicator;
    private String module;
    private int createdBy;
    private Date creationTimestamp;
    private int lastUpdatedBy;
    private Date lastUpdateTimestamp;
    private String state;

    private boolean editDisabled;
    private boolean deleteDisabled;
    private boolean discardDisabled;

    public int getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(int codeListId) {
        this.codeListId = codeListId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getEnumTypeGuid() {
        return enumTypeGuid;
    }

    public void setEnumTypeGuid(String enumTypeGuid) {
        this.enumTypeGuid = enumTypeGuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public int getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(int agencyId) {
        this.agencyId = agencyId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDefinitionSource() {
        return definitionSource;
    }

    public void setDefinitionSource(String definitionSource) {
        this.definitionSource = definitionSource;
    }

    public int getBasedCodeListId() {
        return basedCodeListId;
    }

    public void setBasedCodeListId(int basedCodeListId) {
        this.basedCodeListId = basedCodeListId;
    }

    public boolean isExtensibleIndicator() {
        return extensibleIndicator;
    }

    public void setExtensibleIndicator(boolean extensibleIndicator) {
        this.extensibleIndicator = extensibleIndicator;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public int getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(int lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;

        switch (state) {
            case SRTConstants.CODE_LIST_STATE_EDITING:
                setEditDisabled(false);
                setDiscardDisabled(false);
                setDeleteDisabled(true);
                break;
            case SRTConstants.CODE_LIST_STATE_PUBLISHED:
                setEditDisabled(true);
                setDiscardDisabled(true);
                setDeleteDisabled(false);
                break;
            default:
                setEditDisabled(true);
                setDiscardDisabled(true);
                setDeleteDisabled(true);
        }
    }

    public boolean isEditDisabled() {
        return editDisabled;
    }

    public void setEditDisabled(boolean editDisabled) {
        this.editDisabled = editDisabled;
    }

    public boolean isDeleteDisabled() {
        return deleteDisabled;
    }

    public void setDeleteDisabled(boolean deleteDisabled) {
        this.deleteDisabled = deleteDisabled;
    }

    public boolean isDiscardDisabled() {
        return discardDisabled;
    }

    public void setDiscardDisabled(boolean discardDisabled) {
        this.discardDisabled = discardDisabled;
    }

    @Override
    public String toString() {
        return "CodeList{" +
                "codeListId=" + codeListId +
                ", guid='" + guid + '\'' +
                ", enumTypeGuid='" + enumTypeGuid + '\'' +
                ", name='" + name + '\'' +
                ", listId='" + listId + '\'' +
                ", agencyId=" + agencyId +
                ", versionId='" + versionId + '\'' +
                ", definition='" + definition + '\'' +
                ", remark='" + remark + '\'' +
                ", definitionSource='" + definitionSource + '\'' +
                ", basedCodeListId=" + basedCodeListId +
                ", extensibleIndicator=" + extensibleIndicator +
                ", module='" + module + '\'' +
                ", createdBy=" + createdBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", state='" + state + '\'' +
                ", editDisabled=" + editDisabled +
                ", deleteDisabled=" + deleteDisabled +
                ", discardDisabled=" + discardDisabled +
                '}';
    }
}
