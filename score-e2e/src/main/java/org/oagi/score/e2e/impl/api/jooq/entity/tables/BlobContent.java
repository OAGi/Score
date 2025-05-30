/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables;


import java.util.Collection;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.InverseForeignKey;
import org.jooq.Name;
import org.jooq.Path;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.Keys;
import org.oagi.score.e2e.impl.api.jooq.entity.Oagi;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.BlobContentManifest.BlobContentManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.BlobContentRecord;


/**
 * This table stores schemas whose content is only imported as a whole and is
 * represented in Blob.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class BlobContent extends TableImpl<BlobContentRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.blob_content</code>
     */
    public static final BlobContent BLOB_CONTENT = new BlobContent();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BlobContentRecord> getRecordType() {
        return BlobContentRecord.class;
    }

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
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private BlobContent(Name alias, Table<BlobContentRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment("This table stores schemas whose content is only imported as a whole and is represented in Blob."), TableOptions.table(), where);
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

    public <O extends Record> BlobContent(Table<O> path, ForeignKey<O, BlobContentRecord> childPath, InverseForeignKey<O, BlobContentRecord> parentPath) {
        super(path, childPath, parentPath, BLOB_CONTENT);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class BlobContentPath extends BlobContent implements Path<BlobContentRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> BlobContentPath(Table<O> path, ForeignKey<O, BlobContentRecord> childPath, InverseForeignKey<O, BlobContentRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private BlobContentPath(Name alias, Table<BlobContentRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public BlobContentPath as(String alias) {
            return new BlobContentPath(DSL.name(alias), this);
        }

        @Override
        public BlobContentPath as(Name alias) {
            return new BlobContentPath(alias, this);
        }

        @Override
        public BlobContentPath as(Table<?> alias) {
            return new BlobContentPath(alias.getQualifiedName(), this);
        }
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

    private transient BlobContentManifestPath _blobContentManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.blob_content_manifest</code> table
     */
    public BlobContentManifestPath blobContentManifest() {
        if (_blobContentManifest == null)
            _blobContentManifest = new BlobContentManifestPath(this, null, Keys.BLOB_CONTENT_MANIFEST_BLOB_CONTENT_ID_FK.getInverseKey());

        return _blobContentManifest;
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

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BlobContent where(Condition condition) {
        return new BlobContent(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BlobContent where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BlobContent where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BlobContent where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BlobContent where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BlobContent where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BlobContent where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BlobContent where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BlobContent whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BlobContent whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
