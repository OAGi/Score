/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Asbiep;


/**
 * ASBIEP represents a role in a usage of an ABIE. It is a contextualization of
 * an ASCCP.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AsbiepRecord extends UpdatableRecordImpl<AsbiepRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.asbiep.asbiep_id</code>. A internal, primary
     * database key of an ASBIEP.
     */
    public void setAsbiepId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.asbiep.asbiep_id</code>. A internal, primary
     * database key of an ASBIEP.
     */
    public ULong getAsbiepId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.asbiep.guid</code>. A globally unique identifier
     * (GUID).
     */
    public void setGuid(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.asbiep.guid</code>. A globally unique identifier
     * (GUID).
     */
    public String getGuid() {
        return (String) get(1);
    }

    /**
     * Setter for <code>oagi.asbiep.based_asccp_manifest_id</code>. A foreign
     * key pointing to the ASCCP_MANIFEST record. It is the ASCCP, on which the
     * ASBIEP contextualizes.
     */
    public void setBasedAsccpManifestId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.asbiep.based_asccp_manifest_id</code>. A foreign
     * key pointing to the ASCCP_MANIFEST record. It is the ASCCP, on which the
     * ASBIEP contextualizes.
     */
    public ULong getBasedAsccpManifestId() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.asbiep.path</code>.
     */
    public void setPath(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.asbiep.path</code>.
     */
    public String getPath() {
        return (String) get(3);
    }

    /**
     * Setter for <code>oagi.asbiep.hash_path</code>. hash_path generated from
     * the path of the component graph using hash function, so that it is unique
     * in the graph.
     */
    public void setHashPath(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.asbiep.hash_path</code>. hash_path generated from
     * the path of the component graph using hash function, so that it is unique
     * in the graph.
     */
    public String getHashPath() {
        return (String) get(4);
    }

    /**
     * Setter for <code>oagi.asbiep.role_of_abie_id</code>. A foreign key
     * pointing to the ABIE record. It is the ABIE, which the property term in
     * the based ASCCP qualifies. Note that the ABIE has to be derived from the
     * ACC used by the based ASCCP.
     */
    public void setRoleOfAbieId(ULong value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.asbiep.role_of_abie_id</code>. A foreign key
     * pointing to the ABIE record. It is the ABIE, which the property term in
     * the based ASCCP qualifies. Note that the ABIE has to be derived from the
     * ACC used by the based ASCCP.
     */
    public ULong getRoleOfAbieId() {
        return (ULong) get(5);
    }

    /**
     * Setter for <code>oagi.asbiep.definition</code>. A definition to override
     * the ASCCP's definition. If NULL, it means that the definition should be
     * derived from the based ASCCP on the UI, expression generation, and any
     * API.
     */
    public void setDefinition(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.asbiep.definition</code>. A definition to override
     * the ASCCP's definition. If NULL, it means that the definition should be
     * derived from the based ASCCP on the UI, expression generation, and any
     * API.
     */
    public String getDefinition() {
        return (String) get(6);
    }

    /**
     * Setter for <code>oagi.asbiep.remark</code>. This column allows the user
     * to specify a context-specific usage of the BIE. It is different from the
     * DEFINITION column in that the DEFINITION column is a description
     * conveying the meaning of the associated concept. Remarks may be a very
     * implementation specific instruction or others. For example, BOM BOD, as
     * an ACC, is a generic BOM structure. In a particular context, a BOM ASBIEP
     * can be a Super BOM. Explanation of the Super BOM concept should be
     * captured in the Definition of the ASBIEP. A remark about that ASBIEP may
     * be "Type of BOM should be recognized in the BOM/typeCode."
     */
    public void setRemark(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>oagi.asbiep.remark</code>. This column allows the user
     * to specify a context-specific usage of the BIE. It is different from the
     * DEFINITION column in that the DEFINITION column is a description
     * conveying the meaning of the associated concept. Remarks may be a very
     * implementation specific instruction or others. For example, BOM BOD, as
     * an ACC, is a generic BOM structure. In a particular context, a BOM ASBIEP
     * can be a Super BOM. Explanation of the Super BOM concept should be
     * captured in the Definition of the ASBIEP. A remark about that ASBIEP may
     * be "Type of BOM should be recognized in the BOM/typeCode."
     */
    public String getRemark() {
        return (String) get(7);
    }

    /**
     * Setter for <code>oagi.asbiep.biz_term</code>. This column represents a
     * business term to indicate what the BIE is called in a particular business
     * context. With this current design, only one business term is allowed per
     * business context.
     */
    public void setBizTerm(String value) {
        set(8, value);
    }

    /**
     * Getter for <code>oagi.asbiep.biz_term</code>. This column represents a
     * business term to indicate what the BIE is called in a particular business
     * context. With this current design, only one business term is allowed per
     * business context.
     */
    public String getBizTerm() {
        return (String) get(8);
    }

    /**
     * Setter for <code>oagi.asbiep.created_by</code>. A foreign key referring
     * to the user who creates the ASBIEP. The creator of the ASBIEP is also its
     * owner by default. ASBIEPs created as children of another ABIE have the
     * same CREATED_BY.
     */
    public void setCreatedBy(ULong value) {
        set(9, value);
    }

    /**
     * Getter for <code>oagi.asbiep.created_by</code>. A foreign key referring
     * to the user who creates the ASBIEP. The creator of the ASBIEP is also its
     * owner by default. ASBIEPs created as children of another ABIE have the
     * same CREATED_BY.
     */
    public ULong getCreatedBy() {
        return (ULong) get(9);
    }

    /**
     * Setter for <code>oagi.asbiep.last_updated_by</code>. A foreign key
     * referring to the last user who has updated the ASBIEP record. 
     */
    public void setLastUpdatedBy(ULong value) {
        set(10, value);
    }

    /**
     * Getter for <code>oagi.asbiep.last_updated_by</code>. A foreign key
     * referring to the last user who has updated the ASBIEP record. 
     */
    public ULong getLastUpdatedBy() {
        return (ULong) get(10);
    }

    /**
     * Setter for <code>oagi.asbiep.creation_timestamp</code>. Timestamp when
     * the ASBIEP record was first created. ASBIEPs created as children of
     * another ABIE have the same CREATION_TIMESTAMP.
     */
    public void setCreationTimestamp(LocalDateTime value) {
        set(11, value);
    }

    /**
     * Getter for <code>oagi.asbiep.creation_timestamp</code>. Timestamp when
     * the ASBIEP record was first created. ASBIEPs created as children of
     * another ABIE have the same CREATION_TIMESTAMP.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(11);
    }

    /**
     * Setter for <code>oagi.asbiep.last_update_timestamp</code>. The timestamp
     * when the ASBIEP was last updated.
     */
    public void setLastUpdateTimestamp(LocalDateTime value) {
        set(12, value);
    }

    /**
     * Getter for <code>oagi.asbiep.last_update_timestamp</code>. The timestamp
     * when the ASBIEP was last updated.
     */
    public LocalDateTime getLastUpdateTimestamp() {
        return (LocalDateTime) get(12);
    }

    /**
     * Setter for <code>oagi.asbiep.owner_top_level_asbiep_id</code>. This is a
     * foreign key to the top-level ASBIEP.
     */
    public void setOwnerTopLevelAsbiepId(ULong value) {
        set(13, value);
    }

    /**
     * Getter for <code>oagi.asbiep.owner_top_level_asbiep_id</code>. This is a
     * foreign key to the top-level ASBIEP.
     */
    public ULong getOwnerTopLevelAsbiepId() {
        return (ULong) get(13);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<ULong> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached AsbiepRecord
     */
    public AsbiepRecord() {
        super(Asbiep.ASBIEP);
    }

    /**
     * Create a detached, initialised AsbiepRecord
     */
    public AsbiepRecord(ULong asbiepId, String guid, ULong basedAsccpManifestId, String path, String hashPath, ULong roleOfAbieId, String definition, String remark, String bizTerm, ULong createdBy, ULong lastUpdatedBy, LocalDateTime creationTimestamp, LocalDateTime lastUpdateTimestamp, ULong ownerTopLevelAsbiepId) {
        super(Asbiep.ASBIEP);

        setAsbiepId(asbiepId);
        setGuid(guid);
        setBasedAsccpManifestId(basedAsccpManifestId);
        setPath(path);
        setHashPath(hashPath);
        setRoleOfAbieId(roleOfAbieId);
        setDefinition(definition);
        setRemark(remark);
        setBizTerm(bizTerm);
        setCreatedBy(createdBy);
        setLastUpdatedBy(lastUpdatedBy);
        setCreationTimestamp(creationTimestamp);
        setLastUpdateTimestamp(lastUpdateTimestamp);
        setOwnerTopLevelAsbiepId(ownerTopLevelAsbiepId);
        resetChangedOnNotNull();
    }
}
