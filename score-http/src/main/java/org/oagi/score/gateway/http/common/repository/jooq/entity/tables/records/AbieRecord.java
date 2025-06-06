/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records;


import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Abie;


/**
 * The ABIE table stores information about an ABIE, which is a contextualized
 * ACC. The context is represented by the BUSINESS_CTX_ID column that refers to
 * a business context. Each ABIE must have a business context and a based ACC.
 * 
 * It should be noted that, per design document, there is no corresponding ABIE
 * created for an ACC which will not show up in the instance document such as
 * ACCs of OAGIS_COMPONENT_TYPE "SEMANTIC_GROUP", "USER_EXTENSION_GROUP", etc.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class AbieRecord extends UpdatableRecordImpl<AbieRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.abie.abie_id</code>. A internal, primary database
     * key of an ABIE.
     */
    public void setAbieId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.abie.abie_id</code>. A internal, primary database
     * key of an ABIE.
     */
    public ULong getAbieId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.abie.guid</code>. A globally unique identifier
     * (GUID).
     */
    public void setGuid(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.abie.guid</code>. A globally unique identifier
     * (GUID).
     */
    public String getGuid() {
        return (String) get(1);
    }

    /**
     * Setter for <code>oagi.abie.based_acc_manifest_id</code>. A foreign key to
     * the ACC_MANIFEST table refering to the ACC, on which the business context
     * has been applied to derive this ABIE.
     */
    public void setBasedAccManifestId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.abie.based_acc_manifest_id</code>. A foreign key to
     * the ACC_MANIFEST table refering to the ACC, on which the business context
     * has been applied to derive this ABIE.
     */
    public ULong getBasedAccManifestId() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.abie.path</code>.
     */
    public void setPath(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.abie.path</code>.
     */
    public String getPath() {
        return (String) get(3);
    }

    /**
     * Setter for <code>oagi.abie.hash_path</code>. hash_path generated from the
     * path of the component graph using hash function, so that it is unique in
     * the graph.
     */
    public void setHashPath(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.abie.hash_path</code>. hash_path generated from the
     * path of the component graph using hash function, so that it is unique in
     * the graph.
     */
    public String getHashPath() {
        return (String) get(4);
    }

    /**
     * Setter for <code>oagi.abie.biz_ctx_id</code>. (Deprecated) A foreign key
     * to the BIZ_CTX table. This column stores the business context assigned to
     * the ABIE.
     */
    public void setBizCtxId(ULong value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.abie.biz_ctx_id</code>. (Deprecated) A foreign key
     * to the BIZ_CTX table. This column stores the business context assigned to
     * the ABIE.
     */
    public ULong getBizCtxId() {
        return (ULong) get(5);
    }

    /**
     * Setter for <code>oagi.abie.definition</code>. Definition to override the
     * ACC's definition. If NULL, it means that the definition should be
     * inherited from the based CC.
     */
    public void setDefinition(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.abie.definition</code>. Definition to override the
     * ACC's definition. If NULL, it means that the definition should be
     * inherited from the based CC.
     */
    public String getDefinition() {
        return (String) get(6);
    }

    /**
     * Setter for <code>oagi.abie.created_by</code>. A foreign key referring to
     * the user who creates the ABIE. The creator of the ABIE is also its owner
     * by default. ABIEs created as children of another ABIE have the same
     * CREATED_BY as its parent.
     */
    public void setCreatedBy(ULong value) {
        set(7, value);
    }

    /**
     * Getter for <code>oagi.abie.created_by</code>. A foreign key referring to
     * the user who creates the ABIE. The creator of the ABIE is also its owner
     * by default. ABIEs created as children of another ABIE have the same
     * CREATED_BY as its parent.
     */
    public ULong getCreatedBy() {
        return (ULong) get(7);
    }

    /**
     * Setter for <code>oagi.abie.last_updated_by</code>. A foreign key
     * referring to the last user who has updated the ABIE record. This may be
     * the user who is in the same group as the creator.
     */
    public void setLastUpdatedBy(ULong value) {
        set(8, value);
    }

    /**
     * Getter for <code>oagi.abie.last_updated_by</code>. A foreign key
     * referring to the last user who has updated the ABIE record. This may be
     * the user who is in the same group as the creator.
     */
    public ULong getLastUpdatedBy() {
        return (ULong) get(8);
    }

    /**
     * Setter for <code>oagi.abie.creation_timestamp</code>. Timestamp when the
     * ABIE record was first created. ABIEs created as children of another ABIE
     * have the same CREATION_TIMESTAMP.
     */
    public void setCreationTimestamp(LocalDateTime value) {
        set(9, value);
    }

    /**
     * Getter for <code>oagi.abie.creation_timestamp</code>. Timestamp when the
     * ABIE record was first created. ABIEs created as children of another ABIE
     * have the same CREATION_TIMESTAMP.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(9);
    }

    /**
     * Setter for <code>oagi.abie.last_update_timestamp</code>. The timestamp
     * when the ABIE was last updated.
     */
    public void setLastUpdateTimestamp(LocalDateTime value) {
        set(10, value);
    }

    /**
     * Getter for <code>oagi.abie.last_update_timestamp</code>. The timestamp
     * when the ABIE was last updated.
     */
    public LocalDateTime getLastUpdateTimestamp() {
        return (LocalDateTime) get(10);
    }

    /**
     * Setter for <code>oagi.abie.state</code>. 2 = EDITING, 4 = PUBLISHED. This
     * column is only used with a top-level ABIE, because that is the only entry
     * point for editing. The state value indicates the visibility of the
     * top-level ABIE to users other than the owner. In the user group
     * environment, a logic can apply that other users in the group can see the
     * top-level ABIE only when it is in the 'Published' state.
     */
    public void setState(Integer value) {
        set(11, value);
    }

    /**
     * Getter for <code>oagi.abie.state</code>. 2 = EDITING, 4 = PUBLISHED. This
     * column is only used with a top-level ABIE, because that is the only entry
     * point for editing. The state value indicates the visibility of the
     * top-level ABIE to users other than the owner. In the user group
     * environment, a logic can apply that other users in the group can see the
     * top-level ABIE only when it is in the 'Published' state.
     */
    public Integer getState() {
        return (Integer) get(11);
    }

    /**
     * Setter for <code>oagi.abie.remark</code>. This column allows the user to
     * specify very context-specific usage of the BIE. It is different from the
     * DEFINITION column in that the DEFINITION column is a description
     * conveying the meaning of the associated concept. Remarks may be a very
     * implementation specific instruction or others. For example, BOM BOD, as
     * an ACC, is a generic BOM structure. In a particular context, a BOM ABIE
     * can be a Super BOM. Explanation of the Super BOM concept should be
     * captured in the Definition of the ABIE. A remark about that ABIE may be
     * "Type of BOM should be recognized in the BOM/typeCode."
     */
    public void setRemark(String value) {
        set(12, value);
    }

    /**
     * Getter for <code>oagi.abie.remark</code>. This column allows the user to
     * specify very context-specific usage of the BIE. It is different from the
     * DEFINITION column in that the DEFINITION column is a description
     * conveying the meaning of the associated concept. Remarks may be a very
     * implementation specific instruction or others. For example, BOM BOD, as
     * an ACC, is a generic BOM structure. In a particular context, a BOM ABIE
     * can be a Super BOM. Explanation of the Super BOM concept should be
     * captured in the Definition of the ABIE. A remark about that ABIE may be
     * "Type of BOM should be recognized in the BOM/typeCode."
     */
    public String getRemark() {
        return (String) get(12);
    }

    /**
     * Setter for <code>oagi.abie.biz_term</code>. To indicate what the BIE is
     * called in a particular business context. With this current design, only
     * one business term is allowed per business context.
     */
    public void setBizTerm(String value) {
        set(13, value);
    }

    /**
     * Getter for <code>oagi.abie.biz_term</code>. To indicate what the BIE is
     * called in a particular business context. With this current design, only
     * one business term is allowed per business context.
     */
    public String getBizTerm() {
        return (String) get(13);
    }

    /**
     * Setter for <code>oagi.abie.owner_top_level_asbiep_id</code>. This is a
     * foreign key to the top-level ASBIEP.
     */
    public void setOwnerTopLevelAsbiepId(ULong value) {
        set(14, value);
    }

    /**
     * Getter for <code>oagi.abie.owner_top_level_asbiep_id</code>. This is a
     * foreign key to the top-level ASBIEP.
     */
    public ULong getOwnerTopLevelAsbiepId() {
        return (ULong) get(14);
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
     * Create a detached AbieRecord
     */
    public AbieRecord() {
        super(Abie.ABIE);
    }

    /**
     * Create a detached, initialised AbieRecord
     */
    public AbieRecord(ULong abieId, String guid, ULong basedAccManifestId, String path, String hashPath, ULong bizCtxId, String definition, ULong createdBy, ULong lastUpdatedBy, LocalDateTime creationTimestamp, LocalDateTime lastUpdateTimestamp, Integer state, String remark, String bizTerm, ULong ownerTopLevelAsbiepId) {
        super(Abie.ABIE);

        setAbieId(abieId);
        setGuid(guid);
        setBasedAccManifestId(basedAccManifestId);
        setPath(path);
        setHashPath(hashPath);
        setBizCtxId(bizCtxId);
        setDefinition(definition);
        setCreatedBy(createdBy);
        setLastUpdatedBy(lastUpdatedBy);
        setCreationTimestamp(creationTimestamp);
        setLastUpdateTimestamp(lastUpdateTimestamp);
        setState(state);
        setRemark(remark);
        setBizTerm(bizTerm);
        setOwnerTopLevelAsbiepId(ownerTopLevelAsbiepId);
        resetTouchedOnNotNull();
    }
}
