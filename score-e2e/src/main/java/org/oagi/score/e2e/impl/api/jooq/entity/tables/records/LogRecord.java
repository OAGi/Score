/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Log;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class LogRecord extends UpdatableRecordImpl<LogRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.log.log_id</code>.
     */
    public void setLogId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.log.log_id</code>.
     */
    public ULong getLogId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.log.hash</code>. The unique hash to identify the
     * log.
     */
    public void setHash(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.log.hash</code>. The unique hash to identify the
     * log.
     */
    public String getHash() {
        return (String) get(1);
    }

    /**
     * Setter for <code>oagi.log.revision_num</code>. This is an incremental
     * integer. It tracks changes in each component. If a change is made to a
     * component after it has been published, the component receives a new
     * revision number. Revision number can be 1, 2, and so on.
     */
    public void setRevisionNum(UInteger value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.log.revision_num</code>. This is an incremental
     * integer. It tracks changes in each component. If a change is made to a
     * component after it has been published, the component receives a new
     * revision number. Revision number can be 1, 2, and so on.
     */
    public UInteger getRevisionNum() {
        return (UInteger) get(2);
    }

    /**
     * Setter for <code>oagi.log.revision_tracking_num</code>. This supports the
     * ability to undo changes during a revision (life cycle of a revision is
     * from the component's WIP state to PUBLISHED state). REVISION_TRACKING_NUM
     * can be 1, 2, and so on.
     */
    public void setRevisionTrackingNum(UInteger value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.log.revision_tracking_num</code>. This supports the
     * ability to undo changes during a revision (life cycle of a revision is
     * from the component's WIP state to PUBLISHED state). REVISION_TRACKING_NUM
     * can be 1, 2, and so on.
     */
    public UInteger getRevisionTrackingNum() {
        return (UInteger) get(3);
    }

    /**
     * Setter for <code>oagi.log.log_action</code>. This indicates the action
     * associated with the record.
     */
    public void setLogAction(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.log.log_action</code>. This indicates the action
     * associated with the record.
     */
    public String getLogAction() {
        return (String) get(4);
    }

    /**
     * Setter for <code>oagi.log.reference</code>.
     */
    public void setReference(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.log.reference</code>.
     */
    public String getReference() {
        return (String) get(5);
    }

    /**
     * Setter for <code>oagi.log.snapshot</code>.
     */
    public void setSnapshot(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.log.snapshot</code>.
     */
    public String getSnapshot() {
        return (String) get(6);
    }

    /**
     * Setter for <code>oagi.log.prev_log_id</code>.
     */
    public void setPrevLogId(ULong value) {
        set(7, value);
    }

    /**
     * Getter for <code>oagi.log.prev_log_id</code>.
     */
    public ULong getPrevLogId() {
        return (ULong) get(7);
    }

    /**
     * Setter for <code>oagi.log.next_log_id</code>.
     */
    public void setNextLogId(ULong value) {
        set(8, value);
    }

    /**
     * Getter for <code>oagi.log.next_log_id</code>.
     */
    public ULong getNextLogId() {
        return (ULong) get(8);
    }

    /**
     * Setter for <code>oagi.log.created_by</code>.
     */
    public void setCreatedBy(ULong value) {
        set(9, value);
    }

    /**
     * Getter for <code>oagi.log.created_by</code>.
     */
    public ULong getCreatedBy() {
        return (ULong) get(9);
    }

    /**
     * Setter for <code>oagi.log.creation_timestamp</code>.
     */
    public void setCreationTimestamp(LocalDateTime value) {
        set(10, value);
    }

    /**
     * Getter for <code>oagi.log.creation_timestamp</code>.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(10);
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
     * Create a detached LogRecord
     */
    public LogRecord() {
        super(Log.LOG);
    }

    /**
     * Create a detached, initialised LogRecord
     */
    public LogRecord(ULong logId, String hash, UInteger revisionNum, UInteger revisionTrackingNum, String logAction, String reference, String snapshot, ULong prevLogId, ULong nextLogId, ULong createdBy, LocalDateTime creationTimestamp) {
        super(Log.LOG);

        setLogId(logId);
        setHash(hash);
        setRevisionNum(revisionNum);
        setRevisionTrackingNum(revisionTrackingNum);
        setLogAction(logAction);
        setReference(reference);
        setSnapshot(snapshot);
        setPrevLogId(prevLogId);
        setNextLogId(nextLogId);
        setCreatedBy(createdBy);
        setCreationTimestamp(creationTimestamp);
        resetTouchedOnNotNull();
    }
}
