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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.DtManifest.DtManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Tag.TagPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.DtManifestTagRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DtManifestTag extends TableImpl<DtManifestTagRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.dt_manifest_tag</code>
     */
    public static final DtManifestTag DT_MANIFEST_TAG = new DtManifestTag();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<DtManifestTagRecord> getRecordType() {
        return DtManifestTagRecord.class;
    }

    /**
     * The column <code>oagi.dt_manifest_tag.dt_manifest_id</code>.
     */
    public final TableField<DtManifestTagRecord, ULong> DT_MANIFEST_ID = createField(DSL.name("dt_manifest_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.dt_manifest_tag.tag_id</code>.
     */
    public final TableField<DtManifestTagRecord, ULong> TAG_ID = createField(DSL.name("tag_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.dt_manifest_tag.created_by</code>. A foreign key
     * referring to the user who creates the record.
     */
    public final TableField<DtManifestTagRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key referring to the user who creates the record.");

    /**
     * The column <code>oagi.dt_manifest_tag.creation_timestamp</code>.
     * Timestamp when the record was first created.
     */
    public final TableField<DtManifestTagRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "Timestamp when the record was first created.");

    private DtManifestTag(Name alias, Table<DtManifestTagRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private DtManifestTag(Name alias, Table<DtManifestTagRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.dt_manifest_tag</code> table reference
     */
    public DtManifestTag(String alias) {
        this(DSL.name(alias), DT_MANIFEST_TAG);
    }

    /**
     * Create an aliased <code>oagi.dt_manifest_tag</code> table reference
     */
    public DtManifestTag(Name alias) {
        this(alias, DT_MANIFEST_TAG);
    }

    /**
     * Create a <code>oagi.dt_manifest_tag</code> table reference
     */
    public DtManifestTag() {
        this(DSL.name("dt_manifest_tag"), null);
    }

    public <O extends Record> DtManifestTag(Table<O> path, ForeignKey<O, DtManifestTagRecord> childPath, InverseForeignKey<O, DtManifestTagRecord> parentPath) {
        super(path, childPath, parentPath, DT_MANIFEST_TAG);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class DtManifestTagPath extends DtManifestTag implements Path<DtManifestTagRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> DtManifestTagPath(Table<O> path, ForeignKey<O, DtManifestTagRecord> childPath, InverseForeignKey<O, DtManifestTagRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private DtManifestTagPath(Name alias, Table<DtManifestTagRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public DtManifestTagPath as(String alias) {
            return new DtManifestTagPath(DSL.name(alias), this);
        }

        @Override
        public DtManifestTagPath as(Name alias) {
            return new DtManifestTagPath(alias, this);
        }

        @Override
        public DtManifestTagPath as(Table<?> alias) {
            return new DtManifestTagPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public UniqueKey<DtManifestTagRecord> getPrimaryKey() {
        return Keys.KEY_DT_MANIFEST_TAG_PRIMARY;
    }

    @Override
    public List<ForeignKey<DtManifestTagRecord, ?>> getReferences() {
        return Arrays.asList(Keys.DT_MANIFEST_TAG_DT_MANIFEST_ID_FK, Keys.DT_MANIFEST_TAG_TAG_ID_FK, Keys.DT_MANIFEST_TAG_CREATED_BY_FK);
    }

    private transient DtManifestPath _dtManifest;

    /**
     * Get the implicit join path to the <code>oagi.dt_manifest</code> table.
     */
    public DtManifestPath dtManifest() {
        if (_dtManifest == null)
            _dtManifest = new DtManifestPath(this, Keys.DT_MANIFEST_TAG_DT_MANIFEST_ID_FK, null);

        return _dtManifest;
    }

    private transient TagPath _tag;

    /**
     * Get the implicit join path to the <code>oagi.tag</code> table.
     */
    public TagPath tag() {
        if (_tag == null)
            _tag = new TagPath(this, Keys.DT_MANIFEST_TAG_TAG_ID_FK, null);

        return _tag;
    }

    private transient AppUserPath _appUser;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table.
     */
    public AppUserPath appUser() {
        if (_appUser == null)
            _appUser = new AppUserPath(this, Keys.DT_MANIFEST_TAG_CREATED_BY_FK, null);

        return _appUser;
    }

    @Override
    public DtManifestTag as(String alias) {
        return new DtManifestTag(DSL.name(alias), this);
    }

    @Override
    public DtManifestTag as(Name alias) {
        return new DtManifestTag(alias, this);
    }

    @Override
    public DtManifestTag as(Table<?> alias) {
        return new DtManifestTag(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public DtManifestTag rename(String name) {
        return new DtManifestTag(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public DtManifestTag rename(Name name) {
        return new DtManifestTag(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public DtManifestTag rename(Table<?> name) {
        return new DtManifestTag(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public DtManifestTag where(Condition condition) {
        return new DtManifestTag(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public DtManifestTag where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public DtManifestTag where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public DtManifestTag where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public DtManifestTag where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public DtManifestTag where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public DtManifestTag where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public DtManifestTag where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public DtManifestTag whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public DtManifestTag whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
