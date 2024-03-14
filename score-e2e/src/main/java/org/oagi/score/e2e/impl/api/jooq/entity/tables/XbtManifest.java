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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Log.LogPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.ModuleXbtManifest.ModuleXbtManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Release.ReleasePath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Xbt.XbtPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.XbtManifest.XbtManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.XbtManifestRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class XbtManifest extends TableImpl<XbtManifestRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.xbt_manifest</code>
     */
    public static final XbtManifest XBT_MANIFEST = new XbtManifest();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<XbtManifestRecord> getRecordType() {
        return XbtManifestRecord.class;
    }

    /**
     * The column <code>oagi.xbt_manifest.xbt_manifest_id</code>.
     */
    public final TableField<XbtManifestRecord, ULong> XBT_MANIFEST_ID = createField(DSL.name("xbt_manifest_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "");

    /**
     * The column <code>oagi.xbt_manifest.release_id</code>.
     */
    public final TableField<XbtManifestRecord, ULong> RELEASE_ID = createField(DSL.name("release_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.xbt_manifest.xbt_id</code>.
     */
    public final TableField<XbtManifestRecord, ULong> XBT_ID = createField(DSL.name("xbt_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.xbt_manifest.conflict</code>. This indicates that
     * there is a conflict between self and relationship.
     */
    public final TableField<XbtManifestRecord, Byte> CONFLICT = createField(DSL.name("conflict"), SQLDataType.TINYINT.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.TINYINT)), this, "This indicates that there is a conflict between self and relationship.");

    /**
     * The column <code>oagi.xbt_manifest.log_id</code>. A foreign key pointed
     * to a log for the current record.
     */
    public final TableField<XbtManifestRecord, ULong> LOG_ID = createField(DSL.name("log_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "A foreign key pointed to a log for the current record.");

    /**
     * The column <code>oagi.xbt_manifest.prev_xbt_manifest_id</code>.
     */
    public final TableField<XbtManifestRecord, ULong> PREV_XBT_MANIFEST_ID = createField(DSL.name("prev_xbt_manifest_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "");

    /**
     * The column <code>oagi.xbt_manifest.next_xbt_manifest_id</code>.
     */
    public final TableField<XbtManifestRecord, ULong> NEXT_XBT_MANIFEST_ID = createField(DSL.name("next_xbt_manifest_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "");

    private XbtManifest(Name alias, Table<XbtManifestRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private XbtManifest(Name alias, Table<XbtManifestRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.xbt_manifest</code> table reference
     */
    public XbtManifest(String alias) {
        this(DSL.name(alias), XBT_MANIFEST);
    }

    /**
     * Create an aliased <code>oagi.xbt_manifest</code> table reference
     */
    public XbtManifest(Name alias) {
        this(alias, XBT_MANIFEST);
    }

    /**
     * Create a <code>oagi.xbt_manifest</code> table reference
     */
    public XbtManifest() {
        this(DSL.name("xbt_manifest"), null);
    }

    public <O extends Record> XbtManifest(Table<O> path, ForeignKey<O, XbtManifestRecord> childPath, InverseForeignKey<O, XbtManifestRecord> parentPath) {
        super(path, childPath, parentPath, XBT_MANIFEST);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class XbtManifestPath extends XbtManifest implements Path<XbtManifestRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> XbtManifestPath(Table<O> path, ForeignKey<O, XbtManifestRecord> childPath, InverseForeignKey<O, XbtManifestRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private XbtManifestPath(Name alias, Table<XbtManifestRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public XbtManifestPath as(String alias) {
            return new XbtManifestPath(DSL.name(alias), this);
        }

        @Override
        public XbtManifestPath as(Name alias) {
            return new XbtManifestPath(alias, this);
        }

        @Override
        public XbtManifestPath as(Table<?> alias) {
            return new XbtManifestPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<XbtManifestRecord, ULong> getIdentity() {
        return (Identity<XbtManifestRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<XbtManifestRecord> getPrimaryKey() {
        return Keys.KEY_XBT_MANIFEST_PRIMARY;
    }

    @Override
    public List<ForeignKey<XbtManifestRecord, ?>> getReferences() {
        return Arrays.asList(Keys.XBT_MANIFEST_RELEASE_ID_FK, Keys.XBT_MANIFEST_XBT_ID_FK, Keys.XBT_MANIFEST_LOG_ID_FK, Keys.XBT_MANIFEST_PREV_XBT_MANIFEST_ID_FK, Keys.XBT_MANIFEST_NEXT_XBT_MANIFEST_ID_FK);
    }

    private transient ReleasePath _release;

    /**
     * Get the implicit join path to the <code>oagi.release</code> table.
     */
    public ReleasePath release() {
        if (_release == null)
            _release = new ReleasePath(this, Keys.XBT_MANIFEST_RELEASE_ID_FK, null);

        return _release;
    }

    private transient XbtPath _xbt;

    /**
     * Get the implicit join path to the <code>oagi.xbt</code> table.
     */
    public XbtPath xbt() {
        if (_xbt == null)
            _xbt = new XbtPath(this, Keys.XBT_MANIFEST_XBT_ID_FK, null);

        return _xbt;
    }

    private transient LogPath _log;

    /**
     * Get the implicit join path to the <code>oagi.log</code> table.
     */
    public LogPath log() {
        if (_log == null)
            _log = new LogPath(this, Keys.XBT_MANIFEST_LOG_ID_FK, null);

        return _log;
    }

    private transient XbtManifestPath _xbtManifestPrevXbtManifestIdFk;

    /**
     * Get the implicit join path to the <code>oagi.xbt_manifest</code> table,
     * via the <code>xbt_manifest_prev_xbt_manifest_id_fk</code> key.
     */
    public XbtManifestPath xbtManifestPrevXbtManifestIdFk() {
        if (_xbtManifestPrevXbtManifestIdFk == null)
            _xbtManifestPrevXbtManifestIdFk = new XbtManifestPath(this, Keys.XBT_MANIFEST_PREV_XBT_MANIFEST_ID_FK, null);

        return _xbtManifestPrevXbtManifestIdFk;
    }

    private transient XbtManifestPath _xbtManifestNextXbtManifestIdFk;

    /**
     * Get the implicit join path to the <code>oagi.xbt_manifest</code> table,
     * via the <code>xbt_manifest_next_xbt_manifest_id_fk</code> key.
     */
    public XbtManifestPath xbtManifestNextXbtManifestIdFk() {
        if (_xbtManifestNextXbtManifestIdFk == null)
            _xbtManifestNextXbtManifestIdFk = new XbtManifestPath(this, Keys.XBT_MANIFEST_NEXT_XBT_MANIFEST_ID_FK, null);

        return _xbtManifestNextXbtManifestIdFk;
    }

    private transient ModuleXbtManifestPath _moduleXbtManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.module_xbt_manifest</code> table
     */
    public ModuleXbtManifestPath moduleXbtManifest() {
        if (_moduleXbtManifest == null)
            _moduleXbtManifest = new ModuleXbtManifestPath(this, null, Keys.MODULE_XBT_MANIFEST_BCCP_MANIFEST_ID_FK.getInverseKey());

        return _moduleXbtManifest;
    }

    @Override
    public XbtManifest as(String alias) {
        return new XbtManifest(DSL.name(alias), this);
    }

    @Override
    public XbtManifest as(Name alias) {
        return new XbtManifest(alias, this);
    }

    @Override
    public XbtManifest as(Table<?> alias) {
        return new XbtManifest(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public XbtManifest rename(String name) {
        return new XbtManifest(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public XbtManifest rename(Name name) {
        return new XbtManifest(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public XbtManifest rename(Table<?> name) {
        return new XbtManifest(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public XbtManifest where(Condition condition) {
        return new XbtManifest(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public XbtManifest where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public XbtManifest where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public XbtManifest where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public XbtManifest where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public XbtManifest where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public XbtManifest where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public XbtManifest where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public XbtManifest whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public XbtManifest whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
