package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.common.SRTConstants;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "code_list")
public class CodeList implements Serializable {

    public enum State {
        Editing,
        Published,
        Discarded,
        Deleted
    }

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

    @Column(nullable = false, length = 41)
    private String guid;

    @Column
    private String enumTypeGuid;

    @Column(length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String listId;

    @Column
    private Integer agencyId;

    @Column(nullable = false, length = 10)
    private String versionId;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(length = 225)
    private String remark;

    @Column
    private String definitionSource;

    @Column
    private Integer basedCodeListId;

    @Column(nullable = false)
    private boolean extensibleIndicator;

    @Column(length = 100)
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

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private State state;

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
        return (agencyId == null) ? 0 : agencyId;
    }

    public void setAgencyId(int agencyId) {
        if (agencyId > 0) {
            this.agencyId = agencyId;
        }
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;

        switch (state) {
            case Editing:
                setEditDisabled(false);
                setDiscardDisabled(false);
                setDeleteDisabled(true);
                break;
            case Published:
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CodeList codeList = (CodeList) o;

        if (codeListId != codeList.codeListId) return false;
        if (extensibleIndicator != codeList.extensibleIndicator) return false;
        if (createdBy != codeList.createdBy) return false;
        if (lastUpdatedBy != codeList.lastUpdatedBy) return false;
        if (editDisabled != codeList.editDisabled) return false;
        if (deleteDisabled != codeList.deleteDisabled) return false;
        if (discardDisabled != codeList.discardDisabled) return false;
        if (guid != null ? !guid.equals(codeList.guid) : codeList.guid != null) return false;
        if (enumTypeGuid != null ? !enumTypeGuid.equals(codeList.enumTypeGuid) : codeList.enumTypeGuid != null)
            return false;
        if (name != null ? !name.equals(codeList.name) : codeList.name != null) return false;
        if (listId != null ? !listId.equals(codeList.listId) : codeList.listId != null) return false;
        if (agencyId != null ? !agencyId.equals(codeList.agencyId) : codeList.agencyId != null) return false;
        if (versionId != null ? !versionId.equals(codeList.versionId) : codeList.versionId != null) return false;
        if (definition != null ? !definition.equals(codeList.definition) : codeList.definition != null) return false;
        if (remark != null ? !remark.equals(codeList.remark) : codeList.remark != null) return false;
        if (definitionSource != null ? !definitionSource.equals(codeList.definitionSource) : codeList.definitionSource != null)
            return false;
        if (basedCodeListId != null ? !basedCodeListId.equals(codeList.basedCodeListId) : codeList.basedCodeListId != null)
            return false;
        if (module != null ? !module.equals(codeList.module) : codeList.module != null) return false;
        if (creationTimestamp != null ? !creationTimestamp.equals(codeList.creationTimestamp) : codeList.creationTimestamp != null)
            return false;
        if (lastUpdateTimestamp != null ? !lastUpdateTimestamp.equals(codeList.lastUpdateTimestamp) : codeList.lastUpdateTimestamp != null)
            return false;
        return state != null ? state.equals(codeList.state) : codeList.state == null;

    }

    @Override
    public int hashCode() {
        int result = codeListId;
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (enumTypeGuid != null ? enumTypeGuid.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (listId != null ? listId.hashCode() : 0);
        result = 31 * result + (agencyId != null ? agencyId.hashCode() : 0);
        result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (definitionSource != null ? definitionSource.hashCode() : 0);
        result = 31 * result + (basedCodeListId != null ? basedCodeListId.hashCode() : 0);
        result = 31 * result + (extensibleIndicator ? 1 : 0);
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + createdBy;
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + lastUpdatedBy;
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (editDisabled ? 1 : 0);
        result = 31 * result + (deleteDisabled ? 1 : 0);
        result = 31 * result + (discardDisabled ? 1 : 0);
        return result;
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
