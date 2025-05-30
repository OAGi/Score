/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.gateway.http.common.repository.jooq.entity.tables;


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
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BccpManifest.BccpManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Dt.DtPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.DtManifest.DtManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.DtManifestTag.DtManifestTagPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.DtScManifest.DtScManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Log.LogPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.ModuleDtManifest.ModuleDtManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Release.ReleasePath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Tag.TagPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.DtManifestRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class DtManifest extends TableImpl<DtManifestRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.dt_manifest</code>
     */
    public static final DtManifest DT_MANIFEST = new DtManifest();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<DtManifestRecord> getRecordType() {
        return DtManifestRecord.class;
    }

    /**
     * The column <code>oagi.dt_manifest.dt_manifest_id</code>.
     */
    public final TableField<DtManifestRecord, ULong> DT_MANIFEST_ID = createField(DSL.name("dt_manifest_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "");

    /**
     * The column <code>oagi.dt_manifest.release_id</code>.
     */
    public final TableField<DtManifestRecord, ULong> RELEASE_ID = createField(DSL.name("release_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.dt_manifest.dt_id</code>.
     */
    public final TableField<DtManifestRecord, ULong> DT_ID = createField(DSL.name("dt_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.dt_manifest.based_dt_manifest_id</code>.
     */
    public final TableField<DtManifestRecord, ULong> BASED_DT_MANIFEST_ID = createField(DSL.name("based_dt_manifest_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "");

    /**
     * The column <code>oagi.dt_manifest.den</code>. Dictionary Entry Name of
     * the data type.
     */
    public final TableField<DtManifestRecord, String> DEN = createField(DSL.name("den"), SQLDataType.VARCHAR(200).nullable(false), this, "Dictionary Entry Name of the data type.");

    /**
     * The column <code>oagi.dt_manifest.conflict</code>. This indicates that
     * there is a conflict between self and relationship.
     */
    public final TableField<DtManifestRecord, Byte> CONFLICT = createField(DSL.name("conflict"), SQLDataType.TINYINT.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.TINYINT)), this, "This indicates that there is a conflict between self and relationship.");

    /**
     * The column <code>oagi.dt_manifest.log_id</code>. A foreign key pointed to
     * a log for the current record.
     */
    public final TableField<DtManifestRecord, ULong> LOG_ID = createField(DSL.name("log_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "A foreign key pointed to a log for the current record.");

    /**
     * The column <code>oagi.dt_manifest.replacement_dt_manifest_id</code>. This
     * refers to a replacement manifest if the record is deprecated.
     */
    public final TableField<DtManifestRecord, ULong> REPLACEMENT_DT_MANIFEST_ID = createField(DSL.name("replacement_dt_manifest_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "This refers to a replacement manifest if the record is deprecated.");

    /**
     * The column <code>oagi.dt_manifest.prev_dt_manifest_id</code>.
     */
    public final TableField<DtManifestRecord, ULong> PREV_DT_MANIFEST_ID = createField(DSL.name("prev_dt_manifest_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "");

    /**
     * The column <code>oagi.dt_manifest.next_dt_manifest_id</code>.
     */
    public final TableField<DtManifestRecord, ULong> NEXT_DT_MANIFEST_ID = createField(DSL.name("next_dt_manifest_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "");

    private DtManifest(Name alias, Table<DtManifestRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private DtManifest(Name alias, Table<DtManifestRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.dt_manifest</code> table reference
     */
    public DtManifest(String alias) {
        this(DSL.name(alias), DT_MANIFEST);
    }

    /**
     * Create an aliased <code>oagi.dt_manifest</code> table reference
     */
    public DtManifest(Name alias) {
        this(alias, DT_MANIFEST);
    }

    /**
     * Create a <code>oagi.dt_manifest</code> table reference
     */
    public DtManifest() {
        this(DSL.name("dt_manifest"), null);
    }

    public <O extends Record> DtManifest(Table<O> path, ForeignKey<O, DtManifestRecord> childPath, InverseForeignKey<O, DtManifestRecord> parentPath) {
        super(path, childPath, parentPath, DT_MANIFEST);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class DtManifestPath extends DtManifest implements Path<DtManifestRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> DtManifestPath(Table<O> path, ForeignKey<O, DtManifestRecord> childPath, InverseForeignKey<O, DtManifestRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private DtManifestPath(Name alias, Table<DtManifestRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public DtManifestPath as(String alias) {
            return new DtManifestPath(DSL.name(alias), this);
        }

        @Override
        public DtManifestPath as(Name alias) {
            return new DtManifestPath(alias, this);
        }

        @Override
        public DtManifestPath as(Table<?> alias) {
            return new DtManifestPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<DtManifestRecord, ULong> getIdentity() {
        return (Identity<DtManifestRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<DtManifestRecord> getPrimaryKey() {
        return Keys.KEY_DT_MANIFEST_PRIMARY;
    }

    @Override
    public List<ForeignKey<DtManifestRecord, ?>> getReferences() {
        return Arrays.asList(Keys.DT_MANIFEST_BASED_DT_MANIFEST_ID_FK, Keys.DT_MANIFEST_DT_ID_FK, Keys.DT_MANIFEST_LOG_ID_FK, Keys.DT_MANIFEST_NEXT_DT_MANIFEST_ID_FK, Keys.DT_MANIFEST_PREV_DT_MANIFEST_ID_FK, Keys.DT_MANIFEST_RELEASE_ID_FK, Keys.DT_REPLACEMENT_DT_MANIFEST_ID_FK);
    }

    private transient DtManifestPath _dtManifestBasedDtManifestIdFk;

    /**
     * Get the implicit join path to the <code>oagi.dt_manifest</code> table,
     * via the <code>dt_manifest_based_dt_manifest_id_fk</code> key.
     */
    public DtManifestPath dtManifestBasedDtManifestIdFk() {
        if (_dtManifestBasedDtManifestIdFk == null)
            _dtManifestBasedDtManifestIdFk = new DtManifestPath(this, Keys.DT_MANIFEST_BASED_DT_MANIFEST_ID_FK, null);

        return _dtManifestBasedDtManifestIdFk;
    }

    private transient DtPath _dt;

    /**
     * Get the implicit join path to the <code>oagi.dt</code> table.
     */
    public DtPath dt() {
        if (_dt == null)
            _dt = new DtPath(this, Keys.DT_MANIFEST_DT_ID_FK, null);

        return _dt;
    }

    private transient LogPath _log;

    /**
     * Get the implicit join path to the <code>oagi.log</code> table.
     */
    public LogPath log() {
        if (_log == null)
            _log = new LogPath(this, Keys.DT_MANIFEST_LOG_ID_FK, null);

        return _log;
    }

    private transient DtManifestPath _dtManifestNextDtManifestIdFk;

    /**
     * Get the implicit join path to the <code>oagi.dt_manifest</code> table,
     * via the <code>dt_manifest_next_dt_manifest_id_fk</code> key.
     */
    public DtManifestPath dtManifestNextDtManifestIdFk() {
        if (_dtManifestNextDtManifestIdFk == null)
            _dtManifestNextDtManifestIdFk = new DtManifestPath(this, Keys.DT_MANIFEST_NEXT_DT_MANIFEST_ID_FK, null);

        return _dtManifestNextDtManifestIdFk;
    }

    private transient DtManifestPath _dtManifestPrevDtManifestIdFk;

    /**
     * Get the implicit join path to the <code>oagi.dt_manifest</code> table,
     * via the <code>dt_manifest_prev_dt_manifest_id_fk</code> key.
     */
    public DtManifestPath dtManifestPrevDtManifestIdFk() {
        if (_dtManifestPrevDtManifestIdFk == null)
            _dtManifestPrevDtManifestIdFk = new DtManifestPath(this, Keys.DT_MANIFEST_PREV_DT_MANIFEST_ID_FK, null);

        return _dtManifestPrevDtManifestIdFk;
    }

    private transient ReleasePath _release;

    /**
     * Get the implicit join path to the <code>oagi.release</code> table.
     */
    public ReleasePath release() {
        if (_release == null)
            _release = new ReleasePath(this, Keys.DT_MANIFEST_RELEASE_ID_FK, null);

        return _release;
    }

    private transient DtManifestPath _dtReplacementDtManifestIdFk;

    /**
     * Get the implicit join path to the <code>oagi.dt_manifest</code> table,
     * via the <code>dt_replacement_dt_manifest_id_fk</code> key.
     */
    public DtManifestPath dtReplacementDtManifestIdFk() {
        if (_dtReplacementDtManifestIdFk == null)
            _dtReplacementDtManifestIdFk = new DtManifestPath(this, Keys.DT_REPLACEMENT_DT_MANIFEST_ID_FK, null);

        return _dtReplacementDtManifestIdFk;
    }

    private transient BccpManifestPath _bccpManifest;

    /**
     * Get the implicit to-many join path to the <code>oagi.bccp_manifest</code>
     * table
     */
    public BccpManifestPath bccpManifest() {
        if (_bccpManifest == null)
            _bccpManifest = new BccpManifestPath(this, null, Keys.BCCP_MANIFEST_BDT_MANIFEST_ID_FK.getInverseKey());

        return _bccpManifest;
    }

    private transient DtManifestTagPath _dtManifestTag;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.dt_manifest_tag</code> table
     */
    public DtManifestTagPath dtManifestTag() {
        if (_dtManifestTag == null)
            _dtManifestTag = new DtManifestTagPath(this, null, Keys.DT_MANIFEST_TAG_DT_MANIFEST_ID_FK.getInverseKey());

        return _dtManifestTag;
    }

    private transient DtScManifestPath _dtScManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.dt_sc_manifest</code> table
     */
    public DtScManifestPath dtScManifest() {
        if (_dtScManifest == null)
            _dtScManifest = new DtScManifestPath(this, null, Keys.DT_SC_MANIFEST_OWNER_DT_MANIFEST_ID_FK.getInverseKey());

        return _dtScManifest;
    }

    private transient ModuleDtManifestPath _moduleDtManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.module_dt_manifest</code> table
     */
    public ModuleDtManifestPath moduleDtManifest() {
        if (_moduleDtManifest == null)
            _moduleDtManifest = new ModuleDtManifestPath(this, null, Keys.MODULE_DT_MANIFEST_DT_MANIFEST_ID_FK.getInverseKey());

        return _moduleDtManifest;
    }

    /**
     * Get the implicit many-to-many join path to the <code>oagi.tag</code>
     * table
     */
    public TagPath tag() {
        return dtManifestTag().tag();
    }

    @Override
    public DtManifest as(String alias) {
        return new DtManifest(DSL.name(alias), this);
    }

    @Override
    public DtManifest as(Name alias) {
        return new DtManifest(alias, this);
    }

    @Override
    public DtManifest as(Table<?> alias) {
        return new DtManifest(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public DtManifest rename(String name) {
        return new DtManifest(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public DtManifest rename(Name name) {
        return new DtManifest(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public DtManifest rename(Table<?> name) {
        return new DtManifest(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public DtManifest where(Condition condition) {
        return new DtManifest(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public DtManifest where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public DtManifest where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public DtManifest where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public DtManifest where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public DtManifest where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public DtManifest where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public DtManifest where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public DtManifest whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public DtManifest whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
