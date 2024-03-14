/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Bbiep;


/**
 * BBIEP represents the usage of basic property in a specific business context.
 * It is a contextualization of a BCCP.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BbiepRecord extends UpdatableRecordImpl<BbiepRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.bbiep.bbiep_id</code>. A internal, primary database
     * key of an BBIEP.
     */
    public void setBbiepId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.bbiep.bbiep_id</code>. A internal, primary database
     * key of an BBIEP.
     */
    public ULong getBbiepId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.bbiep.guid</code>. A globally unique identifier
     * (GUID).
     */
    public void setGuid(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.bbiep.guid</code>. A globally unique identifier
     * (GUID).
     */
    public String getGuid() {
        return (String) get(1);
    }

    /**
     * Setter for <code>oagi.bbiep.based_bccp_manifest_id</code>. A foreign key
     * pointing to the BCCP_MANIFEST record. It is the BCCP, which the BBIEP
     * contextualizes.
     */
    public void setBasedBccpManifestId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.bbiep.based_bccp_manifest_id</code>. A foreign key
     * pointing to the BCCP_MANIFEST record. It is the BCCP, which the BBIEP
     * contextualizes.
     */
    public ULong getBasedBccpManifestId() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.bbiep.path</code>.
     */
    public void setPath(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.bbiep.path</code>.
     */
    public String getPath() {
        return (String) get(3);
    }

    /**
     * Setter for <code>oagi.bbiep.hash_path</code>. hash_path generated from
     * the path of the component graph using hash function, so that it is unique
     * in the graph.
     */
    public void setHashPath(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.bbiep.hash_path</code>. hash_path generated from
     * the path of the component graph using hash function, so that it is unique
     * in the graph.
     */
    public String getHashPath() {
        return (String) get(4);
    }

    /**
     * Setter for <code>oagi.bbiep.definition</code>. Definition to override the
     * BCCP's Definition. If NULLl, it means that the definition should be
     * inherited from the based CC.
     */
    public void setDefinition(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.bbiep.definition</code>. Definition to override the
     * BCCP's Definition. If NULLl, it means that the definition should be
     * inherited from the based CC.
     */
    public String getDefinition() {
        return (String) get(5);
    }

    /**
     * Setter for <code>oagi.bbiep.remark</code>. This column allows the user to
     * specify very context-specific usage of the BIE. It is different from the
     * Definition column in that the DEFINITION column is a description
     * conveying the meaning of the associated concept. Remarks may be a very
     * implementation specific instruction or others. For example, BOM BOD, as
     * an ACC, is a generic BOM structure. In a particular context, a BOM ABIE
     * can be a Super BOM. Explanation of the Super BOM concept should be
     * captured in the Definition of the ABIE. A remark about that ABIE may be
     * "Type of BOM should be recognized in the BOM/typeCode.
     */
    public void setRemark(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.bbiep.remark</code>. This column allows the user to
     * specify very context-specific usage of the BIE. It is different from the
     * Definition column in that the DEFINITION column is a description
     * conveying the meaning of the associated concept. Remarks may be a very
     * implementation specific instruction or others. For example, BOM BOD, as
     * an ACC, is a generic BOM structure. In a particular context, a BOM ABIE
     * can be a Super BOM. Explanation of the Super BOM concept should be
     * captured in the Definition of the ABIE. A remark about that ABIE may be
     * "Type of BOM should be recognized in the BOM/typeCode.
     */
    public String getRemark() {
        return (String) get(6);
    }

    /**
     * Setter for <code>oagi.bbiep.biz_term</code>. Business term to indicate
     * what the BIE is called in a particular business context such as in an
     * industry.
     */
    public void setBizTerm(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>oagi.bbiep.biz_term</code>. Business term to indicate
     * what the BIE is called in a particular business context such as in an
     * industry.
     */
    public String getBizTerm() {
        return (String) get(7);
    }

    /**
     * Setter for <code>oagi.bbiep.created_by</code>. A foreign key referring to
     * the user who creates the BBIEP. The creator of the BBIEP is also its
     * owner by default. BBIEPs created as children of another ABIE have the
     * same CREATED_BY',
     */
    public void setCreatedBy(ULong value) {
        set(8, value);
    }

    /**
     * Getter for <code>oagi.bbiep.created_by</code>. A foreign key referring to
     * the user who creates the BBIEP. The creator of the BBIEP is also its
     * owner by default. BBIEPs created as children of another ABIE have the
     * same CREATED_BY',
     */
    public ULong getCreatedBy() {
        return (ULong) get(8);
    }

    /**
     * Setter for <code>oagi.bbiep.last_updated_by</code>. A foreign key
     * referring to the last user who has updated the BBIEP record. 
     */
    public void setLastUpdatedBy(ULong value) {
        set(9, value);
    }

    /**
     * Getter for <code>oagi.bbiep.last_updated_by</code>. A foreign key
     * referring to the last user who has updated the BBIEP record. 
     */
    public ULong getLastUpdatedBy() {
        return (ULong) get(9);
    }

    /**
     * Setter for <code>oagi.bbiep.creation_timestamp</code>. Timestamp when the
     * BBIEP record was first created. BBIEPs created as children of another
     * ABIE have the same CREATION_TIMESTAMP,
     */
    public void setCreationTimestamp(LocalDateTime value) {
        set(10, value);
    }

    /**
     * Getter for <code>oagi.bbiep.creation_timestamp</code>. Timestamp when the
     * BBIEP record was first created. BBIEPs created as children of another
     * ABIE have the same CREATION_TIMESTAMP,
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(10);
    }

    /**
     * Setter for <code>oagi.bbiep.last_update_timestamp</code>. The timestamp
     * when the BBIEP was last updated.
     */
    public void setLastUpdateTimestamp(LocalDateTime value) {
        set(11, value);
    }

    /**
     * Getter for <code>oagi.bbiep.last_update_timestamp</code>. The timestamp
     * when the BBIEP was last updated.
     */
    public LocalDateTime getLastUpdateTimestamp() {
        return (LocalDateTime) get(11);
    }

    /**
     * Setter for <code>oagi.bbiep.owner_top_level_asbiep_id</code>. This is a
     * foreign key to the top-level ASBIEP.
     */
    public void setOwnerTopLevelAsbiepId(ULong value) {
        set(12, value);
    }

    /**
     * Getter for <code>oagi.bbiep.owner_top_level_asbiep_id</code>. This is a
     * foreign key to the top-level ASBIEP.
     */
    public ULong getOwnerTopLevelAsbiepId() {
        return (ULong) get(12);
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
     * Create a detached BbiepRecord
     */
    public BbiepRecord() {
        super(Bbiep.BBIEP);
    }

    /**
     * Create a detached, initialised BbiepRecord
     */
    public BbiepRecord(ULong bbiepId, String guid, ULong basedBccpManifestId, String path, String hashPath, String definition, String remark, String bizTerm, ULong createdBy, ULong lastUpdatedBy, LocalDateTime creationTimestamp, LocalDateTime lastUpdateTimestamp, ULong ownerTopLevelAsbiepId) {
        super(Bbiep.BBIEP);

        setBbiepId(bbiepId);
        setGuid(guid);
        setBasedBccpManifestId(basedBccpManifestId);
        setPath(path);
        setHashPath(hashPath);
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
