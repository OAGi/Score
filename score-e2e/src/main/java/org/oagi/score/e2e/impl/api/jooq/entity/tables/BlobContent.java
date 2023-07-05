/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables;


import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.Keys;
import org.oagi.score.e2e.impl.api.jooq.entity.Oagi;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.BlobContentRecord;

import java.util.function.Function;


/**
 * This table stores schemas whose content is only imported as a whole and is
 * represented in Blob.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class BlobContent extends TableImpl<BlobContentRecord> {

    /**
     * The reference instance of <code>oagi.blob_content</code>
     */
    public static final BlobContent BLOB_CONTENT = new BlobContent();
    private static final long serialVersionUID = 1L;
    /**
     * The column <code>oagi.blob_content.blob_content_id</code>. Primary,
     * internal database key.
     */
    public final TableField<BlobContentRecord, ULong> BLOB_CONTENT_ID = createField(DSL.name("blob_content_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "Primary, internal database key.");
    /**
     * The column <code>oagi.blob_content.content</code>. The Blob content of
     * the schema file.
     */
    public final TableField<BlobContentRecord, byte[]> CONTENT = createField(DSL.name("content"), SQLDataType.BLOB.nullable(false), this, "The Blob content of the schema file.");

    private BlobContent(Name alias, Table<BlobContentRecord> aliased) {
        this(alias, aliased, null);
    }

    private BlobContent(Name alias, Table<BlobContentRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("This table stores schemas whose content is only imported as a whole and is represented in Blob."), TableOptions.table());
    }

    /**
     * Create an aliased <code>oagi.blob_content</code> table reference
     */
    public BlobContent(String alias) {
        this(DSL.name(alias), BLOB_CONTENT);
    }

    /**
     * Create an aliased <code>oagi.blob_content</code> table reference
     */
    public BlobContent(Name alias) {
        this(alias, BLOB_CONTENT);
    }

    /**
     * Create a <code>oagi.blob_content</code> table reference
     */
    public BlobContent() {
        this(DSL.name("blob_content"), null);
    }

    public <O extends Record> BlobContent(Table<O> child, ForeignKey<O, BlobContentRecord> key) {
        super(child, key, BLOB_CONTENT);
    }

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BlobContentRecord> getRecordType() {
        return BlobContentRecord.class;
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<BlobContentRecord, ULong> getIdentity() {
        return (Identity<BlobContentRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<BlobContentRecord> getPrimaryKey() {
        return Keys.KEY_BLOB_CONTENT_PRIMARY;
    }

    @Override
    public BlobContent as(String alias) {
        return new BlobContent(DSL.name(alias), this);
    }

    @Override
    public BlobContent as(Name alias) {
        return new BlobContent(alias, this);
    }

    @Override
    public BlobContent as(Table<?> alias) {
        return new BlobContent(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public BlobContent rename(String name) {
        return new BlobContent(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public BlobContent rename(Name name) {
        return new BlobContent(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public BlobContent rename(Table<?> name) {
        return new BlobContent(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<ULong, byte[]> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function2<? super ULong, ? super byte[], ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function2<? super ULong, ? super byte[], ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
