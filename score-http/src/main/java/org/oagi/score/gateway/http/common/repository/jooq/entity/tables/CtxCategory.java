/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.gateway.http.common.repository.jooq.entity.tables;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
import org.oagi.score.gateway.http.common.repository.jooq.entity.Keys;
import org.oagi.score.gateway.http.common.repository.jooq.entity.Oagi;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AppUser.AppUserPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.CtxScheme.CtxSchemePath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.CtxCategoryRecord;


/**
 * This table captures the context category. Examples of context categories as
 * described in the CCTS are business process, industry, etc.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class CtxCategory extends TableImpl<CtxCategoryRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.ctx_category</code>
     */
    public static final CtxCategory CTX_CATEGORY = new CtxCategory();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<CtxCategoryRecord> getRecordType() {
        return CtxCategoryRecord.class;
    }

    /**
     * The column <code>oagi.ctx_category.ctx_category_id</code>. Internal,
     * primary, database key.
     */
    public final TableField<CtxCategoryRecord, ULong> CTX_CATEGORY_ID = createField(DSL.name("ctx_category_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "Internal, primary, database key.");

    /**
     * The column <code>oagi.ctx_category.guid</code>. A globally unique
     * identifier (GUID).
     */
    public final TableField<CtxCategoryRecord, String> GUID = createField(DSL.name("guid"), SQLDataType.CHAR(32).nullable(false), this, "A globally unique identifier (GUID).");

    /**
     * The column <code>oagi.ctx_category.name</code>. Short name of the context
     * category.
     */
    public final TableField<CtxCategoryRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(45).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "Short name of the context category.");

    /**
     * The column <code>oagi.ctx_category.description</code>. Explanation of
     * what the context category is.
     */
    public final TableField<CtxCategoryRecord, String> DESCRIPTION = createField(DSL.name("description"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "Explanation of what the context category is.");

    /**
     * The column <code>oagi.ctx_category.created_by</code>. Foreign key to the
     * APP_USER table. It indicates the user who created the context category.
     */
    public final TableField<CtxCategoryRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "Foreign key to the APP_USER table. It indicates the user who created the context category.");

    /**
     * The column <code>oagi.ctx_category.last_updated_by</code>. Foreign key to
     * the APP_USER table. It identifies the user who last updated the context
     * category.
     */
    public final TableField<CtxCategoryRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "Foreign key to the APP_USER table. It identifies the user who last updated the context category.");

    /**
     * The column <code>oagi.ctx_category.creation_timestamp</code>. Timestamp
     * when the context category was created.
     */
    public final TableField<CtxCategoryRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false).defaultValue(DSL.field(DSL.raw("current_timestamp(6)"), SQLDataType.LOCALDATETIME)), this, "Timestamp when the context category was created.");

    /**
     * The column <code>oagi.ctx_category.last_update_timestamp</code>.
     * Timestamp when the context category was last updated.
     */
    public final TableField<CtxCategoryRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false).defaultValue(DSL.field(DSL.raw("current_timestamp(6)"), SQLDataType.LOCALDATETIME)), this, "Timestamp when the context category was last updated.");

    private CtxCategory(Name alias, Table<CtxCategoryRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private CtxCategory(Name alias, Table<CtxCategoryRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment("This table captures the context category. Examples of context categories as described in the CCTS are business process, industry, etc."), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.ctx_category</code> table reference
     */
    public CtxCategory(String alias) {
        this(DSL.name(alias), CTX_CATEGORY);
    }

    /**
     * Create an aliased <code>oagi.ctx_category</code> table reference
     */
    public CtxCategory(Name alias) {
        this(alias, CTX_CATEGORY);
    }

    /**
     * Create a <code>oagi.ctx_category</code> table reference
     */
    public CtxCategory() {
        this(DSL.name("ctx_category"), null);
    }

    public <O extends Record> CtxCategory(Table<O> path, ForeignKey<O, CtxCategoryRecord> childPath, InverseForeignKey<O, CtxCategoryRecord> parentPath) {
        super(path, childPath, parentPath, CTX_CATEGORY);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class CtxCategoryPath extends CtxCategory implements Path<CtxCategoryRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> CtxCategoryPath(Table<O> path, ForeignKey<O, CtxCategoryRecord> childPath, InverseForeignKey<O, CtxCategoryRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private CtxCategoryPath(Name alias, Table<CtxCategoryRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public CtxCategoryPath as(String alias) {
            return new CtxCategoryPath(DSL.name(alias), this);
        }

        @Override
        public CtxCategoryPath as(Name alias) {
            return new CtxCategoryPath(alias, this);
        }

        @Override
        public CtxCategoryPath as(Table<?> alias) {
            return new CtxCategoryPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<CtxCategoryRecord, ULong> getIdentity() {
        return (Identity<CtxCategoryRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<CtxCategoryRecord> getPrimaryKey() {
        return Keys.KEY_CTX_CATEGORY_PRIMARY;
    }

    @Override
    public List<UniqueKey<CtxCategoryRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.KEY_CTX_CATEGORY_CTX_CATEGORY_UK1);
    }

    @Override
    public List<ForeignKey<CtxCategoryRecord, ?>> getReferences() {
        return Arrays.asList(Keys.CTX_CATEGORY_CREATED_BY_FK, Keys.CTX_CATEGORY_LAST_UPDATED_BY_FK);
    }

    private transient AppUserPath _ctxCategoryCreatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>ctx_category_created_by_fk</code> key.
     */
    public AppUserPath ctxCategoryCreatedByFk() {
        if (_ctxCategoryCreatedByFk == null)
            _ctxCategoryCreatedByFk = new AppUserPath(this, Keys.CTX_CATEGORY_CREATED_BY_FK, null);

        return _ctxCategoryCreatedByFk;
    }

    private transient AppUserPath _ctxCategoryLastUpdatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>ctx_category_last_updated_by_fk</code> key.
     */
    public AppUserPath ctxCategoryLastUpdatedByFk() {
        if (_ctxCategoryLastUpdatedByFk == null)
            _ctxCategoryLastUpdatedByFk = new AppUserPath(this, Keys.CTX_CATEGORY_LAST_UPDATED_BY_FK, null);

        return _ctxCategoryLastUpdatedByFk;
    }

    private transient CtxSchemePath _ctxScheme;

    /**
     * Get the implicit to-many join path to the <code>oagi.ctx_scheme</code>
     * table
     */
    public CtxSchemePath ctxScheme() {
        if (_ctxScheme == null)
            _ctxScheme = new CtxSchemePath(this, null, Keys.CTX_SCHEME_CTX_CATEGORY_ID_FK.getInverseKey());

        return _ctxScheme;
    }

    @Override
    public CtxCategory as(String alias) {
        return new CtxCategory(DSL.name(alias), this);
    }

    @Override
    public CtxCategory as(Name alias) {
        return new CtxCategory(alias, this);
    }

    @Override
    public CtxCategory as(Table<?> alias) {
        return new CtxCategory(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public CtxCategory rename(String name) {
        return new CtxCategory(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public CtxCategory rename(Name name) {
        return new CtxCategory(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public CtxCategory rename(Table<?> name) {
        return new CtxCategory(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public CtxCategory where(Condition condition) {
        return new CtxCategory(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public CtxCategory where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public CtxCategory where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public CtxCategory where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public CtxCategory where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public CtxCategory where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public CtxCategory where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public CtxCategory where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public CtxCategory whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public CtxCategory whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
