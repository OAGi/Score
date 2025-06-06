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
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AccManifest.AccManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AppUser.AppUserPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Module.ModulePath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.ModuleSetRelease.ModuleSetReleasePath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.ModuleAccManifestRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class ModuleAccManifest extends TableImpl<ModuleAccManifestRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.module_acc_manifest</code>
     */
    public static final ModuleAccManifest MODULE_ACC_MANIFEST = new ModuleAccManifest();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ModuleAccManifestRecord> getRecordType() {
        return ModuleAccManifestRecord.class;
    }

    /**
     * The column <code>oagi.module_acc_manifest.module_acc_manifest_id</code>.
     * Primary key.
     */
    public final TableField<ModuleAccManifestRecord, ULong> MODULE_ACC_MANIFEST_ID = createField(DSL.name("module_acc_manifest_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "Primary key.");

    /**
     * The column <code>oagi.module_acc_manifest.module_set_release_id</code>. A
     * foreign key of the module set release record.
     */
    public final TableField<ModuleAccManifestRecord, ULong> MODULE_SET_RELEASE_ID = createField(DSL.name("module_set_release_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key of the module set release record.");

    /**
     * The column <code>oagi.module_acc_manifest.acc_manifest_id</code>. A
     * foreign key of the acc manifest record.
     */
    public final TableField<ModuleAccManifestRecord, ULong> ACC_MANIFEST_ID = createField(DSL.name("acc_manifest_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key of the acc manifest record.");

    /**
     * The column <code>oagi.module_acc_manifest.module_id</code>. This
     * indicates a module.
     */
    public final TableField<ModuleAccManifestRecord, ULong> MODULE_ID = createField(DSL.name("module_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "This indicates a module.");

    /**
     * The column <code>oagi.module_acc_manifest.created_by</code>. Foreign key
     * to the APP_USER table. It indicates the user who created this record.
     */
    public final TableField<ModuleAccManifestRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "Foreign key to the APP_USER table. It indicates the user who created this record.");

    /**
     * The column <code>oagi.module_acc_manifest.last_updated_by</code>. Foreign
     * key to the APP_USER table referring to the last user who updated the
     * record.
     */
    public final TableField<ModuleAccManifestRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "Foreign key to the APP_USER table referring to the last user who updated the record.");

    /**
     * The column <code>oagi.module_acc_manifest.creation_timestamp</code>. The
     * timestamp when the record was first created.
     */
    public final TableField<ModuleAccManifestRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record was first created.");

    /**
     * The column <code>oagi.module_acc_manifest.last_update_timestamp</code>.
     * The timestamp when the record was last updated.
     */
    public final TableField<ModuleAccManifestRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record was last updated.");

    private ModuleAccManifest(Name alias, Table<ModuleAccManifestRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private ModuleAccManifest(Name alias, Table<ModuleAccManifestRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.module_acc_manifest</code> table reference
     */
    public ModuleAccManifest(String alias) {
        this(DSL.name(alias), MODULE_ACC_MANIFEST);
    }

    /**
     * Create an aliased <code>oagi.module_acc_manifest</code> table reference
     */
    public ModuleAccManifest(Name alias) {
        this(alias, MODULE_ACC_MANIFEST);
    }

    /**
     * Create a <code>oagi.module_acc_manifest</code> table reference
     */
    public ModuleAccManifest() {
        this(DSL.name("module_acc_manifest"), null);
    }

    public <O extends Record> ModuleAccManifest(Table<O> path, ForeignKey<O, ModuleAccManifestRecord> childPath, InverseForeignKey<O, ModuleAccManifestRecord> parentPath) {
        super(path, childPath, parentPath, MODULE_ACC_MANIFEST);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class ModuleAccManifestPath extends ModuleAccManifest implements Path<ModuleAccManifestRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> ModuleAccManifestPath(Table<O> path, ForeignKey<O, ModuleAccManifestRecord> childPath, InverseForeignKey<O, ModuleAccManifestRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private ModuleAccManifestPath(Name alias, Table<ModuleAccManifestRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public ModuleAccManifestPath as(String alias) {
            return new ModuleAccManifestPath(DSL.name(alias), this);
        }

        @Override
        public ModuleAccManifestPath as(Name alias) {
            return new ModuleAccManifestPath(alias, this);
        }

        @Override
        public ModuleAccManifestPath as(Table<?> alias) {
            return new ModuleAccManifestPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<ModuleAccManifestRecord, ULong> getIdentity() {
        return (Identity<ModuleAccManifestRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<ModuleAccManifestRecord> getPrimaryKey() {
        return Keys.KEY_MODULE_ACC_MANIFEST_PRIMARY;
    }

    @Override
    public List<ForeignKey<ModuleAccManifestRecord, ?>> getReferences() {
        return Arrays.asList(Keys.MODULE_ACC_MANIFEST_ACC_MANIFEST_ID_FK, Keys.MODULE_ACC_MANIFEST_CREATED_BY_FK, Keys.MODULE_ACC_MANIFEST_LAST_UPDATED_BY_FK, Keys.MODULE_ACC_MANIFEST_MODULE_ID_FK, Keys.MODULE_ACC_MANIFEST_MODULE_SET_RELEASE_ID_FK);
    }

    private transient AccManifestPath _accManifest;

    /**
     * Get the implicit join path to the <code>oagi.acc_manifest</code> table.
     */
    public AccManifestPath accManifest() {
        if (_accManifest == null)
            _accManifest = new AccManifestPath(this, Keys.MODULE_ACC_MANIFEST_ACC_MANIFEST_ID_FK, null);

        return _accManifest;
    }

    private transient AppUserPath _moduleAccManifestCreatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>module_acc_manifest_created_by_fk</code> key.
     */
    public AppUserPath moduleAccManifestCreatedByFk() {
        if (_moduleAccManifestCreatedByFk == null)
            _moduleAccManifestCreatedByFk = new AppUserPath(this, Keys.MODULE_ACC_MANIFEST_CREATED_BY_FK, null);

        return _moduleAccManifestCreatedByFk;
    }

    private transient AppUserPath _moduleAccManifestLastUpdatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>module_acc_manifest_last_updated_by_fk</code> key.
     */
    public AppUserPath moduleAccManifestLastUpdatedByFk() {
        if (_moduleAccManifestLastUpdatedByFk == null)
            _moduleAccManifestLastUpdatedByFk = new AppUserPath(this, Keys.MODULE_ACC_MANIFEST_LAST_UPDATED_BY_FK, null);

        return _moduleAccManifestLastUpdatedByFk;
    }

    private transient ModulePath _module;

    /**
     * Get the implicit join path to the <code>oagi.module</code> table.
     */
    public ModulePath module() {
        if (_module == null)
            _module = new ModulePath(this, Keys.MODULE_ACC_MANIFEST_MODULE_ID_FK, null);

        return _module;
    }

    private transient ModuleSetReleasePath _moduleSetRelease;

    /**
     * Get the implicit join path to the <code>oagi.module_set_release</code>
     * table.
     */
    public ModuleSetReleasePath moduleSetRelease() {
        if (_moduleSetRelease == null)
            _moduleSetRelease = new ModuleSetReleasePath(this, Keys.MODULE_ACC_MANIFEST_MODULE_SET_RELEASE_ID_FK, null);

        return _moduleSetRelease;
    }

    @Override
    public ModuleAccManifest as(String alias) {
        return new ModuleAccManifest(DSL.name(alias), this);
    }

    @Override
    public ModuleAccManifest as(Name alias) {
        return new ModuleAccManifest(alias, this);
    }

    @Override
    public ModuleAccManifest as(Table<?> alias) {
        return new ModuleAccManifest(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public ModuleAccManifest rename(String name) {
        return new ModuleAccManifest(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ModuleAccManifest rename(Name name) {
        return new ModuleAccManifest(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public ModuleAccManifest rename(Table<?> name) {
        return new ModuleAccManifest(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public ModuleAccManifest where(Condition condition) {
        return new ModuleAccManifest(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public ModuleAccManifest where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public ModuleAccManifest where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public ModuleAccManifest where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public ModuleAccManifest where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public ModuleAccManifest where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public ModuleAccManifest where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public ModuleAccManifest where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public ModuleAccManifest whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public ModuleAccManifest whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
