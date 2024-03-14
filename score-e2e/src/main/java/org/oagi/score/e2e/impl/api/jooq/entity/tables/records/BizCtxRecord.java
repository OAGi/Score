/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.BizCtx;


/**
 * This table represents a business context. A business context is a combination
 * of one or more business context values.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BizCtxRecord extends UpdatableRecordImpl<BizCtxRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.biz_ctx.biz_ctx_id</code>. Primary, internal
     * database key.
     */
    public void setBizCtxId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.biz_ctx.biz_ctx_id</code>. Primary, internal
     * database key.
     */
    public ULong getBizCtxId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.biz_ctx.guid</code>. A globally unique identifier
     * (GUID).
     */
    public void setGuid(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.biz_ctx.guid</code>. A globally unique identifier
     * (GUID).
     */
    public String getGuid() {
        return (String) get(1);
    }

    /**
     * Setter for <code>oagi.biz_ctx.name</code>. Short, descriptive name of the
     * business context.
     */
    public void setName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.biz_ctx.name</code>. Short, descriptive name of the
     * business context.
     */
    public String getName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>oagi.biz_ctx.created_by</code>. Foreign key to the
     * APP_USER table referring to the user who creates the entity. 
     */
    public void setCreatedBy(ULong value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.biz_ctx.created_by</code>. Foreign key to the
     * APP_USER table referring to the user who creates the entity. 
     */
    public ULong getCreatedBy() {
        return (ULong) get(3);
    }

    /**
     * Setter for <code>oagi.biz_ctx.last_updated_by</code>. Foreign key to the
     * APP_USER table  referring to the last user who has updated the business
     * context.
     */
    public void setLastUpdatedBy(ULong value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.biz_ctx.last_updated_by</code>. Foreign key to the
     * APP_USER table  referring to the last user who has updated the business
     * context.
     */
    public ULong getLastUpdatedBy() {
        return (ULong) get(4);
    }

    /**
     * Setter for <code>oagi.biz_ctx.creation_timestamp</code>. Timestamp when
     * the business context record was first created. 
     */
    public void setCreationTimestamp(LocalDateTime value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.biz_ctx.creation_timestamp</code>. Timestamp when
     * the business context record was first created. 
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(5);
    }

    /**
     * Setter for <code>oagi.biz_ctx.last_update_timestamp</code>. The timestamp
     * when the business context was last updated.
     */
    public void setLastUpdateTimestamp(LocalDateTime value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.biz_ctx.last_update_timestamp</code>. The timestamp
     * when the business context was last updated.
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
     * Create a detached BizCtxRecord
     */
    public BizCtxRecord() {
        super(BizCtx.BIZ_CTX);
    }

    /**
     * Create a detached, initialised BizCtxRecord
     */
    public BizCtxRecord(ULong bizCtxId, String guid, String name, ULong createdBy, ULong lastUpdatedBy, LocalDateTime creationTimestamp, LocalDateTime lastUpdateTimestamp) {
        super(BizCtx.BIZ_CTX);

        setBizCtxId(bizCtxId);
        setGuid(guid);
        setName(name);
        setCreatedBy(createdBy);
        setLastUpdatedBy(lastUpdatedBy);
        setCreationTimestamp(creationTimestamp);
        setLastUpdateTimestamp(lastUpdateTimestamp);
        resetChangedOnNotNull();
    }
}
