/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AsccBizterm;


/**
 * The ascc_bizterm table stores information about the aggregation between the
 * business term and ASCC. TODO: Placeholder, definition is missing.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AsccBiztermRecord extends UpdatableRecordImpl<AsccBiztermRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.ascc_bizterm.ascc_bizterm_id</code>. An internal,
     * primary database key of an Business term.
     */
    public void setAsccBiztermId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.ascc_bizterm.ascc_bizterm_id</code>. An internal,
     * primary database key of an Business term.
     */
    public ULong getAsccBiztermId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.ascc_bizterm.business_term_id</code>. An internal
     * ID of the associated business term
     */
    public void setBusinessTermId(ULong value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.ascc_bizterm.business_term_id</code>. An internal
     * ID of the associated business term
     */
    public ULong getBusinessTermId() {
        return (ULong) get(1);
    }

    /**
     * Setter for <code>oagi.ascc_bizterm.ascc_id</code>. An internal ID of the
     * associated ASCC
     */
    public void setAsccId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.ascc_bizterm.ascc_id</code>. An internal ID of the
     * associated ASCC
     */
    public ULong getAsccId() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.ascc_bizterm.created_by</code>. A foreign key
     * referring to the user who creates the ascc_bizterm record. The creator of
     * the ascc_bizterm is also its owner by default.
     */
    public void setCreatedBy(ULong value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.ascc_bizterm.created_by</code>. A foreign key
     * referring to the user who creates the ascc_bizterm record. The creator of
     * the ascc_bizterm is also its owner by default.
     */
    public ULong getCreatedBy() {
        return (ULong) get(3);
    }

    /**
     * Setter for <code>oagi.ascc_bizterm.last_updated_by</code>. A foreign key
     * referring to the last user who has updated the ascc_bizterm record. This
     * may be the user who is in the same group as the creator.
     */
    public void setLastUpdatedBy(ULong value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.ascc_bizterm.last_updated_by</code>. A foreign key
     * referring to the last user who has updated the ascc_bizterm record. This
     * may be the user who is in the same group as the creator.
     */
    public ULong getLastUpdatedBy() {
        return (ULong) get(4);
    }

    /**
     * Setter for <code>oagi.ascc_bizterm.creation_timestamp</code>. Timestamp
     * when the ascc_bizterm record was first created.
     */
    public void setCreationTimestamp(LocalDateTime value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.ascc_bizterm.creation_timestamp</code>. Timestamp
     * when the ascc_bizterm record was first created.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(5);
    }

    /**
     * Setter for <code>oagi.ascc_bizterm.last_update_timestamp</code>. The
     * timestamp when the ascc_bizterm was last updated.
     */
    public void setLastUpdateTimestamp(LocalDateTime value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.ascc_bizterm.last_update_timestamp</code>. The
     * timestamp when the ascc_bizterm was last updated.
     */
    public LocalDateTime getLastUpdateTimestamp() {
        return (LocalDateTime) get(6);
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
     * Create a detached AsccBiztermRecord
     */
    public AsccBiztermRecord() {
        super(AsccBizterm.ASCC_BIZTERM);
    }

    /**
     * Create a detached, initialised AsccBiztermRecord
     */
    public AsccBiztermRecord(ULong asccBiztermId, ULong businessTermId, ULong asccId, ULong createdBy, ULong lastUpdatedBy, LocalDateTime creationTimestamp, LocalDateTime lastUpdateTimestamp) {
        super(AsccBizterm.ASCC_BIZTERM);

        setAsccBiztermId(asccBiztermId);
        setBusinessTermId(businessTermId);
        setAsccId(asccId);
        setCreatedBy(createdBy);
        setLastUpdatedBy(lastUpdatedBy);
        setCreationTimestamp(creationTimestamp);
        setLastUpdateTimestamp(lastUpdateTimestamp);
        resetChangedOnNotNull();
    }
}
