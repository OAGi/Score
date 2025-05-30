/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables;


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
import org.oagi.score.e2e.impl.api.jooq.entity.Keys;
import org.oagi.score.e2e.impl.api.jooq.entity.Oagi;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AccManifest.AccManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Asbiep.AsbiepPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AsccManifest.AsccManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Asccp.AsccpPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AsccpManifest.AsccpManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AsccpManifestTag.AsccpManifestTagPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Log.LogPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.ModuleAsccpManifest.ModuleAsccpManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Release.ReleasePath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Tag.TagPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.AsccpManifestRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class AsccpManifest extends TableImpl<AsccpManifestRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.asccp_manifest</code>
     */
    public static final AsccpManifest ASCCP_MANIFEST = new AsccpManifest();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AsccpManifestRecord> getRecordType() {
        return AsccpManifestRecord.class;
    }

    /**
     * The column <code>oagi.asccp_manifest.asccp_manifest_id</code>.
     */
    public final TableField<AsccpManifestRecord, ULong> ASCCP_MANIFEST_ID = createField(DSL.name("asccp_manifest_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "");

    /**
     * The column <code>oagi.asccp_manifest.release_id</code>.
     */
    public final TableField<AsccpManifestRecord, ULong> RELEASE_ID = createField(DSL.name("release_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.asccp_manifest.asccp_id</code>.
     */
    public final TableField<AsccpManifestRecord, ULong> ASCCP_ID = createField(DSL.name("asccp_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.asccp_manifest.role_of_acc_manifest_id</code>.
     */
    public final TableField<AsccpManifestRecord, ULong> ROLE_OF_ACC_MANIFEST_ID = createField(DSL.name("role_of_acc_manifest_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.asccp_manifest.den</code>. The dictionary entry
     * name of the ASCCP.
     */
    public final TableField<AsccpManifestRecord, String> DEN = createField(DSL.name("den"), SQLDataType.VARCHAR(202).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "The dictionary entry name of the ASCCP.");

    /**
     * The column <code>oagi.asccp_manifest.conflict</code>. This indicates that
     * there is a conflict between self and relationship.
     */
    public final TableField<AsccpManifestRecord, Byte> CONFLICT = createField(DSL.name("conflict"), SQLDataType.TINYINT.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.TINYINT)), this, "This indicates that there is a conflict between self and relationship.");

    /**
     * The column <code>oagi.asccp_manifest.log_id</code>. A foreign key pointed
     * to a log for the current record.
     */
    public final TableField<AsccpManifestRecord, ULong> LOG_ID = createField(DSL.name("log_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "A foreign key pointed to a log for the current record.");

    /**
     * The column
     * <code>oagi.asccp_manifest.replacement_asccp_manifest_id</code>. This
     * refers to a replacement manifest if the record is deprecated.
     */
    public final TableField<AsccpManifestRecord, ULong> REPLACEMENT_ASCCP_MANIFEST_ID = createField(DSL.name("replacement_asccp_manifest_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "This refers to a replacement manifest if the record is deprecated.");

    /**
     * The column <code>oagi.asccp_manifest.prev_asccp_manifest_id</code>.
     */
    public final TableField<AsccpManifestRecord, ULong> PREV_ASCCP_MANIFEST_ID = createField(DSL.name("prev_asccp_manifest_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "");

    /**
     * The column <code>oagi.asccp_manifest.next_asccp_manifest_id</code>.
     */
    public final TableField<AsccpManifestRecord, ULong> NEXT_ASCCP_MANIFEST_ID = createField(DSL.name("next_asccp_manifest_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "");

    private AsccpManifest(Name alias, Table<AsccpManifestRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private AsccpManifest(Name alias, Table<AsccpManifestRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.asccp_manifest</code> table reference
     */
    public AsccpManifest(String alias) {
        this(DSL.name(alias), ASCCP_MANIFEST);
    }

    /**
     * Create an aliased <code>oagi.asccp_manifest</code> table reference
     */
    public AsccpManifest(Name alias) {
        this(alias, ASCCP_MANIFEST);
    }

    /**
     * Create a <code>oagi.asccp_manifest</code> table reference
     */
    public AsccpManifest() {
        this(DSL.name("asccp_manifest"), null);
    }

    public <O extends Record> AsccpManifest(Table<O> path, ForeignKey<O, AsccpManifestRecord> childPath, InverseForeignKey<O, AsccpManifestRecord> parentPath) {
        super(path, childPath, parentPath, ASCCP_MANIFEST);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class AsccpManifestPath extends AsccpManifest implements Path<AsccpManifestRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> AsccpManifestPath(Table<O> path, ForeignKey<O, AsccpManifestRecord> childPath, InverseForeignKey<O, AsccpManifestRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private AsccpManifestPath(Name alias, Table<AsccpManifestRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public AsccpManifestPath as(String alias) {
            return new AsccpManifestPath(DSL.name(alias), this);
        }

        @Override
        public AsccpManifestPath as(Name alias) {
            return new AsccpManifestPath(alias, this);
        }

        @Override
        public AsccpManifestPath as(Table<?> alias) {
            return new AsccpManifestPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<AsccpManifestRecord, ULong> getIdentity() {
        return (Identity<AsccpManifestRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<AsccpManifestRecord> getPrimaryKey() {
        return Keys.KEY_ASCCP_MANIFEST_PRIMARY;
    }

    @Override
    public List<ForeignKey<AsccpManifestRecord, ?>> getReferences() {
        return Arrays.asList(Keys.ASCCP_MANIFEST_ASCCP_ID_FK, Keys.ASCCP_MANIFEST_LOG_ID_FK, Keys.ASCCP_MANIFEST_NEXT_ASCCP_MANIFEST_ID_FK, Keys.ASCCP_MANIFEST_PREV_ASCCP_MANIFEST_ID_FK, Keys.ASCCP_MANIFEST_RELEASE_ID_FK, Keys.ASCCP_MANIFEST_ROLE_OF_ACC_MANIFEST_ID_FK, Keys.ASCCP_REPLACEMENT_ASCCP_MANIFEST_ID_FK);
    }

    private transient AsccpPath _asccp;

    /**
     * Get the implicit join path to the <code>oagi.asccp</code> table.
     */
    public AsccpPath asccp() {
        if (_asccp == null)
            _asccp = new AsccpPath(this, Keys.ASCCP_MANIFEST_ASCCP_ID_FK, null);

        return _asccp;
    }

    private transient LogPath _log;

    /**
     * Get the implicit join path to the <code>oagi.log</code> table.
     */
    public LogPath log() {
        if (_log == null)
            _log = new LogPath(this, Keys.ASCCP_MANIFEST_LOG_ID_FK, null);

        return _log;
    }

    private transient AsccpManifestPath _asccpManifestNextAsccpManifestIdFk;

    /**
     * Get the implicit join path to the <code>oagi.asccp_manifest</code> table,
     * via the <code>asccp_manifest_next_asccp_manifest_id_fk</code> key.
     */
    public AsccpManifestPath asccpManifestNextAsccpManifestIdFk() {
        if (_asccpManifestNextAsccpManifestIdFk == null)
            _asccpManifestNextAsccpManifestIdFk = new AsccpManifestPath(this, Keys.ASCCP_MANIFEST_NEXT_ASCCP_MANIFEST_ID_FK, null);

        return _asccpManifestNextAsccpManifestIdFk;
    }

    private transient AsccpManifestPath _asccpManifestPrevAsccpManifestIdFk;

    /**
     * Get the implicit join path to the <code>oagi.asccp_manifest</code> table,
     * via the <code>asccp_manifest_prev_asccp_manifest_id_fk</code> key.
     */
    public AsccpManifestPath asccpManifestPrevAsccpManifestIdFk() {
        if (_asccpManifestPrevAsccpManifestIdFk == null)
            _asccpManifestPrevAsccpManifestIdFk = new AsccpManifestPath(this, Keys.ASCCP_MANIFEST_PREV_ASCCP_MANIFEST_ID_FK, null);

        return _asccpManifestPrevAsccpManifestIdFk;
    }

    private transient ReleasePath _release;

    /**
     * Get the implicit join path to the <code>oagi.release</code> table.
     */
    public ReleasePath release() {
        if (_release == null)
            _release = new ReleasePath(this, Keys.ASCCP_MANIFEST_RELEASE_ID_FK, null);

        return _release;
    }

    private transient AccManifestPath _accManifest;

    /**
     * Get the implicit join path to the <code>oagi.acc_manifest</code> table.
     */
    public AccManifestPath accManifest() {
        if (_accManifest == null)
            _accManifest = new AccManifestPath(this, Keys.ASCCP_MANIFEST_ROLE_OF_ACC_MANIFEST_ID_FK, null);

        return _accManifest;
    }

    private transient AsccpManifestPath _asccpReplacementAsccpManifestIdFk;

    /**
     * Get the implicit join path to the <code>oagi.asccp_manifest</code> table,
     * via the <code>asccp_replacement_asccp_manifest_id_fk</code> key.
     */
    public AsccpManifestPath asccpReplacementAsccpManifestIdFk() {
        if (_asccpReplacementAsccpManifestIdFk == null)
            _asccpReplacementAsccpManifestIdFk = new AsccpManifestPath(this, Keys.ASCCP_REPLACEMENT_ASCCP_MANIFEST_ID_FK, null);

        return _asccpReplacementAsccpManifestIdFk;
    }

    private transient AsbiepPath _asbiep;

    /**
     * Get the implicit to-many join path to the <code>oagi.asbiep</code> table
     */
    public AsbiepPath asbiep() {
        if (_asbiep == null)
            _asbiep = new AsbiepPath(this, null, Keys.ASBIEP_BASED_ASCCP_MANIFEST_ID_FK.getInverseKey());

        return _asbiep;
    }

    private transient AsccManifestPath _asccManifest;

    /**
     * Get the implicit to-many join path to the <code>oagi.ascc_manifest</code>
     * table
     */
    public AsccManifestPath asccManifest() {
        if (_asccManifest == null)
            _asccManifest = new AsccManifestPath(this, null, Keys.ASCC_MANIFEST_TO_ASCCP_MANIFEST_ID_FK.getInverseKey());

        return _asccManifest;
    }

    private transient AsccpManifestTagPath _asccpManifestTag;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.asccp_manifest_tag</code> table
     */
    public AsccpManifestTagPath asccpManifestTag() {
        if (_asccpManifestTag == null)
            _asccpManifestTag = new AsccpManifestTagPath(this, null, Keys.ASCCP_MANIFEST_TAG_ASCCP_MANIFEST_ID_FK.getInverseKey());

        return _asccpManifestTag;
    }

    private transient ModuleAsccpManifestPath _moduleAsccpManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.module_asccp_manifest</code> table
     */
    public ModuleAsccpManifestPath moduleAsccpManifest() {
        if (_moduleAsccpManifest == null)
            _moduleAsccpManifest = new ModuleAsccpManifestPath(this, null, Keys.MODULE_ASCCP_MANIFEST_ASCCP_MANIFEST_ID_FK.getInverseKey());

        return _moduleAsccpManifest;
    }

    /**
     * Get the implicit many-to-many join path to the <code>oagi.tag</code>
     * table
     */
    public TagPath tag() {
        return asccpManifestTag().tag();
    }

    @Override
    public AsccpManifest as(String alias) {
        return new AsccpManifest(DSL.name(alias), this);
    }

    @Override
    public AsccpManifest as(Name alias) {
        return new AsccpManifest(alias, this);
    }

    @Override
    public AsccpManifest as(Table<?> alias) {
        return new AsccpManifest(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public AsccpManifest rename(String name) {
        return new AsccpManifest(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public AsccpManifest rename(Name name) {
        return new AsccpManifest(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public AsccpManifest rename(Table<?> name) {
        return new AsccpManifest(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AsccpManifest where(Condition condition) {
        return new AsccpManifest(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AsccpManifest where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AsccpManifest where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AsccpManifest where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public AsccpManifest where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public AsccpManifest where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public AsccpManifest where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public AsccpManifest where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AsccpManifest whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AsccpManifest whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
