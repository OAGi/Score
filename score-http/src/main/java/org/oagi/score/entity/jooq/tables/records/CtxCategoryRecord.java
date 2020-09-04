/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.entity.jooq.tables.records;


import javax.annotation.processing.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.entity.jooq.tables.CtxCategory;


/**
 * This table captures the context category. Examples of context categories 
 * as described in the CCTS are business process, industry, etc.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.4"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CtxCategoryRecord extends UpdatableRecordImpl<CtxCategoryRecord> implements Record4<ULong, String, String, String> {

    private static final long serialVersionUID = -58792976;

    /**
     * Setter for <code>oagi.ctx_category.ctx_category_id</code>. Internal, primary, database key.
     */
    public void setCtxCategoryId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.ctx_category.ctx_category_id</code>. Internal, primary, database key.
     */
    public ULong getCtxCategoryId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.ctx_category.guid</code>. GUID of the context category.  Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.
     */
    public void setGuid(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.ctx_category.guid</code>. GUID of the context category.  Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.
     */
    public String getGuid() {
        return (String) get(1);
    }

    /**
     * Setter for <code>oagi.ctx_category.name</code>. Short name of the context category.
     */
    public void setName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.ctx_category.name</code>. Short name of the context category.
     */
    public String getName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>oagi.ctx_category.description</code>. Explanation of what the context category is.
     */
    public void setDescription(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.ctx_category.description</code>. Explanation of what the context category is.
     */
    public String getDescription() {
        return (String) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<ULong> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<ULong, String, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<ULong, String, String, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<ULong> field1() {
        return CtxCategory.CTX_CATEGORY.CTX_CATEGORY_ID;
    }

    @Override
    public Field<String> field2() {
        return CtxCategory.CTX_CATEGORY.GUID;
    }

    @Override
    public Field<String> field3() {
        return CtxCategory.CTX_CATEGORY.NAME;
    }

    @Override
    public Field<String> field4() {
        return CtxCategory.CTX_CATEGORY.DESCRIPTION;
    }

    @Override
    public ULong component1() {
        return getCtxCategoryId();
    }

    @Override
    public String component2() {
        return getGuid();
    }

    @Override
    public String component3() {
        return getName();
    }

    @Override
    public String component4() {
        return getDescription();
    }

    @Override
    public ULong value1() {
        return getCtxCategoryId();
    }

    @Override
    public String value2() {
        return getGuid();
    }

    @Override
    public String value3() {
        return getName();
    }

    @Override
    public String value4() {
        return getDescription();
    }

    @Override
    public CtxCategoryRecord value1(ULong value) {
        setCtxCategoryId(value);
        return this;
    }

    @Override
    public CtxCategoryRecord value2(String value) {
        setGuid(value);
        return this;
    }

    @Override
    public CtxCategoryRecord value3(String value) {
        setName(value);
        return this;
    }

    @Override
    public CtxCategoryRecord value4(String value) {
        setDescription(value);
        return this;
    }

    @Override
    public CtxCategoryRecord values(ULong value1, String value2, String value3, String value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached CtxCategoryRecord
     */
    public CtxCategoryRecord() {
        super(CtxCategory.CTX_CATEGORY);
    }

    /**
     * Create a detached, initialised CtxCategoryRecord
     */
    public CtxCategoryRecord(ULong ctxCategoryId, String guid, String name, String description) {
        super(CtxCategory.CTX_CATEGORY);

        set(0, ctxCategoryId);
        set(1, guid);
        set(2, name);
        set(3, description);
    }
}
