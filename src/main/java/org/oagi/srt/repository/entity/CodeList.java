package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.common.SRTConstants;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "code_list")
public class CodeList implements Serializable {

    public static final String SEQUENCE_NAME = "CODE_LIST_ID_SEQ";

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
    private int codeListId;

    @Column(nullable = false)
    private String guid;

    @Column
    private String enumTypeGuid;

    @Column
    private String name;

    @Column(nullable = false)
    private String listId;

    @Column(nullable = false)
    private int agencyId;

    @Column(nullable = false)
    private String versionId;

    @Lob
    @Column
    private String definition;

    @Column
    private String remark;

    @Column
    private String definitionSource;

    @Column
    private Integer basedCodeListId;

    @Column(nullable = false)
    private boolean extensibleIndicator;

    @Column
    private String module;

    @Column(nullable = false, updatable = false)
    private int createdBy;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    @Column(nullable = false)
    private int lastUpdatedBy;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

    @Column(nullable = false)
    private String state;

    @Transient
    private boolean editDisabled;

    @Transient
    private boolean deleteDisabled;

    @Transient
    private boolean discardDisabled;

    @PrePersist
    public void prePersist() {
        creationTimestamp = new Date();
        lastUpdateTimestamp = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdateTimestamp = new Date();
    }

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
        return (basedCodeListId == null) ? 0 : basedCodeListId;
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
