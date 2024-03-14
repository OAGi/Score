/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AppUser.AppUserPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.OasDoc.OasDocPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.OasExternalDoc.OasExternalDocPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.OasExternalDocDocRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OasExternalDocDoc extends TableImpl<OasExternalDocDocRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.oas_external_doc_doc</code>
     */
    public static final OasExternalDocDoc OAS_EXTERNAL_DOC_DOC = new OasExternalDocDoc();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OasExternalDocDocRecord> getRecordType() {
        return OasExternalDocDocRecord.class;
    }

    /**
     * The column <code>oagi.oas_external_doc_doc.oas_external_doc_id</code>.
     * The primary key of the record.
     */
    public final TableField<OasExternalDocDocRecord, ULong> OAS_EXTERNAL_DOC_ID = createField(DSL.name("oas_external_doc_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The primary key of the record.");

    /**
     * The column <code>oagi.oas_external_doc_doc.oas_doc_id</code>. The primary
     * key of the record.
     */
    public final TableField<OasExternalDocDocRecord, ULong> OAS_DOC_ID = createField(DSL.name("oas_doc_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The primary key of the record.");

    /**
     * The column <code>oagi.oas_external_doc_doc.created_by</code>. The user
     * who creates the record.
     */
    public final TableField<OasExternalDocDocRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who creates the record.");

    /**
     * The column <code>oagi.oas_external_doc_doc.last_updated_by</code>. The
     * user who last updates the record.
     */
    public final TableField<OasExternalDocDocRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who last updates the record.");

    /**
     * The column <code>oagi.oas_external_doc_doc.creation_timestamp</code>. The
     * timestamp when the record is created.
     */
    public final TableField<OasExternalDocDocRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record is created.");

    /**
     * The column <code>oagi.oas_external_doc_doc.last_update_timestamp</code>.
     * The timestamp when the record is last updated.
     */
    public final TableField<OasExternalDocDocRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record is last updated.");

    private OasExternalDocDoc(Name alias, Table<OasExternalDocDocRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private OasExternalDocDoc(Name alias, Table<OasExternalDocDocRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.oas_external_doc_doc</code> table reference
     */
    public OasExternalDocDoc(String alias) {
        this(DSL.name(alias), OAS_EXTERNAL_DOC_DOC);
    }

    /**
     * Create an aliased <code>oagi.oas_external_doc_doc</code> table reference
     */
    public OasExternalDocDoc(Name alias) {
        this(alias, OAS_EXTERNAL_DOC_DOC);
    }

    /**
     * Create a <code>oagi.oas_external_doc_doc</code> table reference
     */
    public OasExternalDocDoc() {
        this(DSL.name("oas_external_doc_doc"), null);
    }

    public <O extends Record> OasExternalDocDoc(Table<O> path, ForeignKey<O, OasExternalDocDocRecord> childPath, InverseForeignKey<O, OasExternalDocDocRecord> parentPath) {
        super(path, childPath, parentPath, OAS_EXTERNAL_DOC_DOC);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class OasExternalDocDocPath extends OasExternalDocDoc implements Path<OasExternalDocDocRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> OasExternalDocDocPath(Table<O> path, ForeignKey<O, OasExternalDocDocRecord> childPath, InverseForeignKey<O, OasExternalDocDocRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private OasExternalDocDocPath(Name alias, Table<OasExternalDocDocRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public OasExternalDocDocPath as(String alias) {
            return new OasExternalDocDocPath(DSL.name(alias), this);
        }

        @Override
        public OasExternalDocDocPath as(Name alias) {
            return new OasExternalDocDocPath(alias, this);
        }

        @Override
        public OasExternalDocDocPath as(Table<?> alias) {
            return new OasExternalDocDocPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public UniqueKey<OasExternalDocDocRecord> getPrimaryKey() {
        return Keys.KEY_OAS_EXTERNAL_DOC_DOC_PRIMARY;
    }

    @Override
    public List<ForeignKey<OasExternalDocDocRecord, ?>> getReferences() {
        return Arrays.asList(Keys.OAS_EXTERNAL_DOC_OAS_EXTERNAL_DOC_ID_FK, Keys.OAS_EXTERNAL_DOC_OAS_DOC_ID_FK, Keys.OAS_EXTERNAL_DOC_DOC_CREATED_BY_FK, Keys.OAS_EXTERNAL_DOC_DOC_LAST_UPDATED_BY_FK);
    }

    private transient OasExternalDocPath _oasExternalDoc;

    /**
     * Get the implicit join path to the <code>oagi.oas_external_doc</code>
     * table.
     */
    public OasExternalDocPath oasExternalDoc() {
        if (_oasExternalDoc == null)
            _oasExternalDoc = new OasExternalDocPath(this, Keys.OAS_EXTERNAL_DOC_OAS_EXTERNAL_DOC_ID_FK, null);

        return _oasExternalDoc;
    }

    private transient OasDocPath _oasDoc;

    /**
     * Get the implicit join path to the <code>oagi.oas_doc</code> table.
     */
    public OasDocPath oasDoc() {
        if (_oasDoc == null)
            _oasDoc = new OasDocPath(this, Keys.OAS_EXTERNAL_DOC_OAS_DOC_ID_FK, null);

        return _oasDoc;
    }

    private transient AppUserPath _oasExternalDocDocCreatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>oas_external_doc_doc_created_by_fk</code> key.
     */
    public AppUserPath oasExternalDocDocCreatedByFk() {
        if (_oasExternalDocDocCreatedByFk == null)
            _oasExternalDocDocCreatedByFk = new AppUserPath(this, Keys.OAS_EXTERNAL_DOC_DOC_CREATED_BY_FK, null);

        return _oasExternalDocDocCreatedByFk;
    }

    private transient AppUserPath _oasExternalDocDocLastUpdatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>oas_external_doc_doc_last_updated_by_fk</code> key.
     */
    public AppUserPath oasExternalDocDocLastUpdatedByFk() {
        if (_oasExternalDocDocLastUpdatedByFk == null)
            _oasExternalDocDocLastUpdatedByFk = new AppUserPath(this, Keys.OAS_EXTERNAL_DOC_DOC_LAST_UPDATED_BY_FK, null);

        return _oasExternalDocDocLastUpdatedByFk;
    }

    @Override
    public OasExternalDocDoc as(String alias) {
        return new OasExternalDocDoc(DSL.name(alias), this);
    }

    @Override
    public OasExternalDocDoc as(Name alias) {
        return new OasExternalDocDoc(alias, this);
    }

    @Override
    public OasExternalDocDoc as(Table<?> alias) {
        return new OasExternalDocDoc(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public OasExternalDocDoc rename(String name) {
        return new OasExternalDocDoc(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public OasExternalDocDoc rename(Name name) {
        return new OasExternalDocDoc(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public OasExternalDocDoc rename(Table<?> name) {
        return new OasExternalDocDoc(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasExternalDocDoc where(Condition condition) {
        return new OasExternalDocDoc(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasExternalDocDoc where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasExternalDocDoc where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasExternalDocDoc where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasExternalDocDoc where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasExternalDocDoc where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasExternalDocDoc where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasExternalDocDoc where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasExternalDocDoc whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasExternalDocDoc whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
