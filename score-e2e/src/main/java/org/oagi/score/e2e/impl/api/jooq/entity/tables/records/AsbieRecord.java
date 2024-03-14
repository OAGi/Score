/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Asbie;


/**
 * An ASBIE represents a relationship/association between two ABIEs through an
 * ASBIEP. It is a contextualization of an ASCC.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AsbieRecord extends UpdatableRecordImpl<AsbieRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.asbie.asbie_id</code>. A internal, primary database
     * key of an ASBIE.
     */
    public void setAsbieId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.asbie.asbie_id</code>. A internal, primary database
     * key of an ASBIE.
     */
    public ULong getAsbieId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.asbie.guid</code>. A globally unique identifier
     * (GUID).
     */
    public void setGuid(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.asbie.guid</code>. A globally unique identifier
     * (GUID).
     */
    public String getGuid() {
        return (String) get(1);
    }

    /**
     * Setter for <code>oagi.asbie.based_ascc_manifest_id</code>. The
     * BASED_ASCC_MANIFEST_ID column refers to the ASCC_MANIFEST record, which
     * this ASBIE contextualizes.
     */
    public void setBasedAsccManifestId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.asbie.based_ascc_manifest_id</code>. The
     * BASED_ASCC_MANIFEST_ID column refers to the ASCC_MANIFEST record, which
     * this ASBIE contextualizes.
     */
    public ULong getBasedAsccManifestId() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.asbie.path</code>.
     */
    public void setPath(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.asbie.path</code>.
     */
    public String getPath() {
        return (String) get(3);
    }

    /**
     * Setter for <code>oagi.asbie.hash_path</code>. hash_path generated from
     * the path of the component graph using hash function, so that it is unique
     * in the graph.
     */
    public void setHashPath(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.asbie.hash_path</code>. hash_path generated from
     * the path of the component graph using hash function, so that it is unique
     * in the graph.
     */
    public String getHashPath() {
        return (String) get(4);
    }

    /**
     * Setter for <code>oagi.asbie.from_abie_id</code>. A foreign key pointing
     * to the ABIE table. FROM_ABIE_ID is basically  a parent data element
     * (type) of the TO_ASBIEP_ID. FROM_ABIE_ID must be based on the FROM_ACC_ID
     * in the BASED_ASCC_ID except when the FROM_ACC_ID refers to an
     * SEMANTIC_GROUP ACC or USER_EXTENSION_GROUP ACC.
     */
    public void setFromAbieId(ULong value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.asbie.from_abie_id</code>. A foreign key pointing
     * to the ABIE table. FROM_ABIE_ID is basically  a parent data element
     * (type) of the TO_ASBIEP_ID. FROM_ABIE_ID must be based on the FROM_ACC_ID
     * in the BASED_ASCC_ID except when the FROM_ACC_ID refers to an
     * SEMANTIC_GROUP ACC or USER_EXTENSION_GROUP ACC.
     */
    public ULong getFromAbieId() {
        return (ULong) get(5);
    }

    /**
     * Setter for <code>oagi.asbie.to_asbiep_id</code>. A foreign key to the
     * ASBIEP table. TO_ASBIEP_ID is basically a child data element of the
     * FROM_ABIE_ID. The TO_ASBIEP_ID must be based on the TO_ASCCP_ID in the
     * BASED_ASCC_ID. the ASBIEP is reused with the OWNER_TOP_LEVEL_ASBIEP is
     * different after joining ASBIE and ASBIEP tables
     */
    public void setToAsbiepId(ULong value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.asbie.to_asbiep_id</code>. A foreign key to the
     * ASBIEP table. TO_ASBIEP_ID is basically a child data element of the
     * FROM_ABIE_ID. The TO_ASBIEP_ID must be based on the TO_ASCCP_ID in the
     * BASED_ASCC_ID. the ASBIEP is reused with the OWNER_TOP_LEVEL_ASBIEP is
     * different after joining ASBIE and ASBIEP tables
     */
    public ULong getToAsbiepId() {
        return (ULong) get(6);
    }

    /**
     * Setter for <code>oagi.asbie.definition</code>. Definition to override the
     * ASCC definition. If NULL, it means that the definition should be derived
     * from the based CC on the UI, expression generation, and any API.
     */
    public void setDefinition(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>oagi.asbie.definition</code>. Definition to override the
     * ASCC definition. If NULL, it means that the definition should be derived
     * from the based CC on the UI, expression generation, and any API.
     */
    public String getDefinition() {
        return (String) get(7);
    }

    /**
     * Setter for <code>oagi.asbie.cardinality_min</code>. Minimum occurence
     * constraint of the TO_ASBIEP_ID. A valid value is a non-negative integer.
     */
    public void setCardinalityMin(Integer value) {
        set(8, value);
    }

    /**
     * Getter for <code>oagi.asbie.cardinality_min</code>. Minimum occurence
     * constraint of the TO_ASBIEP_ID. A valid value is a non-negative integer.
     */
    public Integer getCardinalityMin() {
        return (Integer) get(8);
    }

    /**
     * Setter for <code>oagi.asbie.cardinality_max</code>. Maximum occurrence
     * constraint of the TO_ASBIEP_ID. A valid value is an integer from -1 and
     * up. Specifically, -1 means unbounded. 0 means prohibited or not to use.
     */
    public void setCardinalityMax(Integer value) {
        set(9, value);
    }

    /**
     * Getter for <code>oagi.asbie.cardinality_max</code>. Maximum occurrence
     * constraint of the TO_ASBIEP_ID. A valid value is an integer from -1 and
     * up. Specifically, -1 means unbounded. 0 means prohibited or not to use.
     */
    public Integer getCardinalityMax() {
        return (Integer) get(9);
    }

    /**
     * Setter for <code>oagi.asbie.is_nillable</code>. Indicate whether the
     * TO_ASBIEP_ID is allowed to be null.
     */
    public void setIsNillable(Byte value) {
        set(10, value);
    }

    /**
     * Getter for <code>oagi.asbie.is_nillable</code>. Indicate whether the
     * TO_ASBIEP_ID is allowed to be null.
     */
    public Byte getIsNillable() {
        return (Byte) get(10);
    }

    /**
     * Setter for <code>oagi.asbie.remark</code>. This column allows the user to
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
        set(11, value);
    }

    /**
     * Getter for <code>oagi.asbie.remark</code>. This column allows the user to
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
        return (String) get(11);
    }

    /**
     * Setter for <code>oagi.asbie.created_by</code>. A foreign key referring to
     * the user who creates the ASBIE. The creator of the ASBIE is also its
     * owner by default. ASBIEs created as children of another ABIE have the
     * same CREATED_BY.
     */
    public void setCreatedBy(ULong value) {
        set(12, value);
    }

    /**
     * Getter for <code>oagi.asbie.created_by</code>. A foreign key referring to
     * the user who creates the ASBIE. The creator of the ASBIE is also its
     * owner by default. ASBIEs created as children of another ABIE have the
     * same CREATED_BY.
     */
    public ULong getCreatedBy() {
        return (ULong) get(12);
    }

    /**
     * Setter for <code>oagi.asbie.last_updated_by</code>. A foreign key
     * referring to the user who has last updated the ASBIE record. 
     */
    public void setLastUpdatedBy(ULong value) {
        set(13, value);
    }

    /**
     * Getter for <code>oagi.asbie.last_updated_by</code>. A foreign key
     * referring to the user who has last updated the ASBIE record. 
     */
    public ULong getLastUpdatedBy() {
        return (ULong) get(13);
    }

    /**
     * Setter for <code>oagi.asbie.creation_timestamp</code>. Timestamp when the
     * ASBIE record was first created. ASBIEs created as children of another
     * ABIE have the same CREATION_TIMESTAMP.
     */
    public void setCreationTimestamp(LocalDateTime value) {
        set(14, value);
    }

    /**
     * Getter for <code>oagi.asbie.creation_timestamp</code>. Timestamp when the
     * ASBIE record was first created. ASBIEs created as children of another
     * ABIE have the same CREATION_TIMESTAMP.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(14);
    }

    /**
     * Setter for <code>oagi.asbie.last_update_timestamp</code>. The timestamp
     * when the ASBIE was last updated.
     */
    public void setLastUpdateTimestamp(LocalDateTime value) {
        set(15, value);
    }

    /**
     * Getter for <code>oagi.asbie.last_update_timestamp</code>. The timestamp
     * when the ASBIE was last updated.
     */
    public LocalDateTime getLastUpdateTimestamp() {
        return (LocalDateTime) get(15);
    }

    /**
     * Setter for <code>oagi.asbie.seq_key</code>. This indicates the order of
     * the associations among other siblings. The SEQ_KEY for BIEs is decimal in
     * order to accomodate the removal of inheritance hierarchy and group. For
     * example, children of the most abstract ACC will have SEQ_KEY = 1.1, 1.2,
     * 1.3, and so on; and SEQ_KEY of the next abstraction level ACC will have
     * SEQ_KEY = 2.1, 2.2, 2.3 and so on so forth.
     */
    public void setSeqKey(BigDecimal value) {
        set(16, value);
    }

    /**
     * Getter for <code>oagi.asbie.seq_key</code>. This indicates the order of
     * the associations among other siblings. The SEQ_KEY for BIEs is decimal in
     * order to accomodate the removal of inheritance hierarchy and group. For
     * example, children of the most abstract ACC will have SEQ_KEY = 1.1, 1.2,
     * 1.3, and so on; and SEQ_KEY of the next abstraction level ACC will have
     * SEQ_KEY = 2.1, 2.2, 2.3 and so on so forth.
     */
    public BigDecimal getSeqKey() {
        return (BigDecimal) get(16);
    }

    /**
     * Setter for <code>oagi.asbie.is_used</code>. Flag to indicate whether the
     * field/component is used in the content model. It signifies whether the
     * field/component should be generated.
     */
    public void setIsUsed(Byte value) {
        set(17, value);
    }

    /**
     * Getter for <code>oagi.asbie.is_used</code>. Flag to indicate whether the
     * field/component is used in the content model. It signifies whether the
     * field/component should be generated.
     */
    public Byte getIsUsed() {
        return (Byte) get(17);
    }

    /**
     * Setter for <code>oagi.asbie.is_deprecated</code>. Indicates whether the
     * ASBIE is deprecated.
     */
    public void setIsDeprecated(Byte value) {
        set(18, value);
    }

    /**
     * Getter for <code>oagi.asbie.is_deprecated</code>. Indicates whether the
     * ASBIE is deprecated.
     */
    public Byte getIsDeprecated() {
        return (Byte) get(18);
    }

    /**
     * Setter for <code>oagi.asbie.owner_top_level_asbiep_id</code>. This is a
     * foreign key to the top-level ASBIEP.
     */
    public void setOwnerTopLevelAsbiepId(ULong value) {
        set(19, value);
    }

    /**
     * Getter for <code>oagi.asbie.owner_top_level_asbiep_id</code>. This is a
     * foreign key to the top-level ASBIEP.
     */
    public ULong getOwnerTopLevelAsbiepId() {
        return (ULong) get(19);
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
     * Create a detached AsbieRecord
     */
    public AsbieRecord() {
        super(Asbie.ASBIE);
    }

    /**
     * Create a detached, initialised AsbieRecord
     */
    public AsbieRecord(ULong asbieId, String guid, ULong basedAsccManifestId, String path, String hashPath, ULong fromAbieId, ULong toAsbiepId, String definition, Integer cardinalityMin, Integer cardinalityMax, Byte isNillable, String remark, ULong createdBy, ULong lastUpdatedBy, LocalDateTime creationTimestamp, LocalDateTime lastUpdateTimestamp, BigDecimal seqKey, Byte isUsed, Byte isDeprecated, ULong ownerTopLevelAsbiepId) {
        super(Asbie.ASBIE);

        setAsbieId(asbieId);
        setGuid(guid);
        setBasedAsccManifestId(basedAsccManifestId);
        setPath(path);
        setHashPath(hashPath);
        setFromAbieId(fromAbieId);
        setToAsbiepId(toAsbiepId);
        setDefinition(definition);
        setCardinalityMin(cardinalityMin);
        setCardinalityMax(cardinalityMax);
        setIsNillable(isNillable);
        setRemark(remark);
        setCreatedBy(createdBy);
        setLastUpdatedBy(lastUpdatedBy);
        setCreationTimestamp(creationTimestamp);
        setLastUpdateTimestamp(lastUpdateTimestamp);
        setSeqKey(seqKey);
        setIsUsed(isUsed);
        setIsDeprecated(isDeprecated);
        setOwnerTopLevelAsbiepId(ownerTopLevelAsbiepId);
        resetChangedOnNotNull();
    }
}
