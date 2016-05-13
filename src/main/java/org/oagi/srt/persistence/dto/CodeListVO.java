package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;
import org.oagi.srt.repository.entity.CodeList;

/**
 * @author Jaehun Lee
 * @version 1.0
 */


public class CodeListVO extends SRTObject {

    private int codeListID;
    private String codeListGUID;
    private String enumerationTypeGUID;
    private String name;
    private String listID;
    private int agencyID;
    private String versionID;
    private String definition;
    private String definitionSource;
    private int basedCodeListID;
    private boolean extensibleIndicator;
    private int createdByUserID;
    private int lastUpdatedByUserID;
    private Timestamp creationTimestamp;
    private Timestamp lastUpdateTimestamp;
    private String state;
    private String remark;
    private boolean editDisabled;
    private boolean deleteDisabled;
    private boolean discardDisabled;

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

    public boolean isEditDisabled() {
        return editDisabled;
    }

    public void setEditDisabled(boolean editDisabled) {
        this.editDisabled = editDisabled;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getCodeListID() {
        return codeListID;
    }

    public void setCodeListID(int codeListID) {
        this.codeListID = codeListID;
    }

    public String getCodeListGUID() {
        return codeListGUID;
    }

    public void setCodeListGUID(String codeListGUID) {
        this.codeListGUID = codeListGUID;
    }

    public String getEnumerationTypeGUID() {
        return enumerationTypeGUID;
    }

    public void setEnumerationTypeGUID(String enumerationTypeGUID) {
        this.enumerationTypeGUID = enumerationTypeGUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getListID() {
        return listID;
    }

    public void setListID(String listID) {
        this.listID = listID;
    }

    public int getAgencyID() {
        return agencyID;
    }

    public void setAgencyID(int agencyID) {
        this.agencyID = agencyID;
    }

    public String getVersionID() {
        return versionID;
    }

    public void setVersionID(String versionID) {
        this.versionID = versionID;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDefinitionSource() {
        return definitionSource;
    }

    public void setDefinitionSource(String definitionSource) {
        this.definitionSource = definitionSource;
    }

    public int getBasedCodeListID() {
        return basedCodeListID;
    }

    public void setBasedCodeListID(int basedCodeListID) {
        this.basedCodeListID = basedCodeListID;
    }

    public boolean getExtensibleIndicator() {
        return extensibleIndicator;
    }

    public void setExtensibleIndicator(boolean extensibleIndicator) {
        this.extensibleIndicator = extensibleIndicator;
    }

    public int getCreatedByUserID() {
        return createdByUserID;
    }

    public void setCreatedByUserID(int createdByUserID) {
        this.createdByUserID = createdByUserID;
    }

    public int getLastUpdatedByUserID() {
        return lastUpdatedByUserID;
    }

    public void setLastUpdatedByUserID(int lastUpdatedByUserID) {
        this.lastUpdatedByUserID = lastUpdatedByUserID;
    }

    public Timestamp getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Timestamp creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Timestamp getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Timestamp lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public static CodeListVO valueOf(CodeList codeList) {
        CodeListVO codeListVO = new CodeListVO();
        codeListVO.setDefinition(codeList.getDefinition());
        codeListVO.setDefinitionSource(codeList.getDefinitionSource());
        codeListVO.setName(codeList.getName());
        codeListVO.setAgencyID(codeList.getAgencyId());
        codeListVO.setBasedCodeListID(codeList.getBasedCodeListId());
        codeListVO.setCodeListGUID(codeList.getGuid());
        codeListVO.setCodeListID(codeList.getCodeListId());
        codeListVO.setCreatedByUserID(codeList.getCreatedBy());
        codeListVO.setCreationTimestamp(new Timestamp(codeList.getCreationTimestamp().getTime()));
        codeListVO.setDeleteDisabled(codeList.isDeleteDisabled());
        codeListVO.setDiscardDisabled(codeList.isDiscardDisabled());
        codeListVO.setEditDisabled(codeList.isEditDisabled());
        codeListVO.setEnumerationTypeGUID(codeList.getEnumTypeGuid());
        codeListVO.setExtensibleIndicator(codeList.isExtensibleIndicator());
        codeListVO.setLastUpdatedByUserID(codeList.getLastUpdatedBy());
        codeListVO.setLastUpdateTimestamp(new Timestamp(codeList.getLastUpdateTimestamp().getTime()));
        codeListVO.setListID(codeList.getListId());
        codeListVO.setName(codeList.getName());
        codeListVO.setRemark(codeList.getRemark());
        codeListVO.setState(codeList.getState());
        codeListVO.setVersionID(codeList.getVersionId());
        return codeListVO;
    }

}
