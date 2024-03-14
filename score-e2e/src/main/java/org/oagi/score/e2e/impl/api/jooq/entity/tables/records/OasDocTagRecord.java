/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import java.time.LocalDateTime;

import org.jooq.Record2;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.OasDocTag;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OasDocTagRecord extends UpdatableRecordImpl<OasDocTagRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.oas_doc_tag.oas_doc_id</code>. The primary key of
     * the record.
     */
    public void setOasDocId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.oas_doc_tag.oas_doc_id</code>. The primary key of
     * the record.
     */
    public ULong getOasDocId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.oas_doc_tag.oas_tag_id</code>. The primary key of
     * the record.
     */
    public void setOasTagId(ULong value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.oas_doc_tag.oas_tag_id</code>. The primary key of
     * the record.
     */
    public ULong getOasTagId() {
        return (ULong) get(1);
    }

    /**
     * Setter for <code>oagi.oas_doc_tag.created_by</code>. The user who creates
     * the record.
     */
    public void setCreatedBy(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.oas_doc_tag.created_by</code>. The user who creates
     * the record.
     */
    public ULong getCreatedBy() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.oas_doc_tag.last_updated_by</code>. The user who
     * last updates the record.
     */
    public void setLastUpdatedBy(ULong value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.oas_doc_tag.last_updated_by</code>. The user who
     * last updates the record.
     */
    public ULong getLastUpdatedBy() {
        return (ULong) get(3);
    }

    /**
     * Setter for <code>oagi.oas_doc_tag.creation_timestamp</code>. The
     * timestamp when the record is created.
     */
    public void setCreationTimestamp(LocalDateTime value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.oas_doc_tag.creation_timestamp</code>. The
     * timestamp when the record is created.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(4);
    }

    /**
     * Setter for <code>oagi.oas_doc_tag.last_update_timestamp</code>. The
     * timestamp when the record is last updated.
     */
    public void setLastUpdateTimestamp(LocalDateTime value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.oas_doc_tag.last_update_timestamp</code>. The
     * timestamp when the record is last updated.
     */
    public LocalDateTime getLastUpdateTimestamp() {
        return (LocalDateTime) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<ULong, ULong> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached OasDocTagRecord
     */
    public OasDocTagRecord() {
        super(OasDocTag.OAS_DOC_TAG);
    }

    /**
     * Create a detached, initialised OasDocTagRecord
     */
    public OasDocTagRecord(ULong oasDocId, ULong oasTagId, ULong createdBy, ULong lastUpdatedBy, LocalDateTime creationTimestamp, LocalDateTime lastUpdateTimestamp) {
        super(OasDocTag.OAS_DOC_TAG);

        setOasDocId(oasDocId);
        setOasTagId(oasTagId);
        setCreatedBy(createdBy);
        setLastUpdatedBy(lastUpdatedBy);
        setCreationTimestamp(creationTimestamp);
        setLastUpdateTimestamp(lastUpdateTimestamp);
        resetChangedOnNotNull();
    }
}
