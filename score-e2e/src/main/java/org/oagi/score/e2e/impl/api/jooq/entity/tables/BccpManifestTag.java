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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.BccpManifest.BccpManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Tag.TagPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.BccpManifestTagRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class BccpManifestTag extends TableImpl<BccpManifestTagRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.bccp_manifest_tag</code>
     */
    public static final BccpManifestTag BCCP_MANIFEST_TAG = new BccpManifestTag();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BccpManifestTagRecord> getRecordType() {
        return BccpManifestTagRecord.class;
    }

    /**
     * The column <code>oagi.bccp_manifest_tag.bccp_manifest_id</code>.
     */
    public final TableField<BccpManifestTagRecord, ULong> BCCP_MANIFEST_ID = createField(DSL.name("bccp_manifest_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.bccp_manifest_tag.tag_id</code>.
     */
    public final TableField<BccpManifestTagRecord, ULong> TAG_ID = createField(DSL.name("tag_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.bccp_manifest_tag.created_by</code>. A foreign key
     * referring to the user who creates the record.
     */
    public final TableField<BccpManifestTagRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key referring to the user who creates the record.");

    /**
     * The column <code>oagi.bccp_manifest_tag.creation_timestamp</code>.
     * Timestamp when the record was first created.
     */
    public final TableField<BccpManifestTagRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "Timestamp when the record was first created.");

    private BccpManifestTag(Name alias, Table<BccpManifestTagRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private BccpManifestTag(Name alias, Table<BccpManifestTagRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.bccp_manifest_tag</code> table reference
     */
    public BccpManifestTag(String alias) {
        this(DSL.name(alias), BCCP_MANIFEST_TAG);
    }

    /**
     * Create an aliased <code>oagi.bccp_manifest_tag</code> table reference
     */
    public BccpManifestTag(Name alias) {
        this(alias, BCCP_MANIFEST_TAG);
    }

    /**
     * Create a <code>oagi.bccp_manifest_tag</code> table reference
     */
    public BccpManifestTag() {
        this(DSL.name("bccp_manifest_tag"), null);
    }

    public <O extends Record> BccpManifestTag(Table<O> path, ForeignKey<O, BccpManifestTagRecord> childPath, InverseForeignKey<O, BccpManifestTagRecord> parentPath) {
        super(path, childPath, parentPath, BCCP_MANIFEST_TAG);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class BccpManifestTagPath extends BccpManifestTag implements Path<BccpManifestTagRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> BccpManifestTagPath(Table<O> path, ForeignKey<O, BccpManifestTagRecord> childPath, InverseForeignKey<O, BccpManifestTagRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private BccpManifestTagPath(Name alias, Table<BccpManifestTagRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public BccpManifestTagPath as(String alias) {
            return new BccpManifestTagPath(DSL.name(alias), this);
        }

        @Override
        public BccpManifestTagPath as(Name alias) {
            return new BccpManifestTagPath(alias, this);
        }

        @Override
        public BccpManifestTagPath as(Table<?> alias) {
            return new BccpManifestTagPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public UniqueKey<BccpManifestTagRecord> getPrimaryKey() {
        return Keys.KEY_BCCP_MANIFEST_TAG_PRIMARY;
    }

    @Override
    public List<ForeignKey<BccpManifestTagRecord, ?>> getReferences() {
        return Arrays.asList(Keys.BCCP_MANIFEST_TAG_BCCP_MANIFEST_ID_FK, Keys.BCCP_MANIFEST_TAG_CREATED_BY_FK, Keys.BCCP_MANIFEST_TAG_TAG_ID_FK);
    }

    private transient BccpManifestPath _bccpManifest;

    /**
     * Get the implicit join path to the <code>oagi.bccp_manifest</code> table.
     */
    public BccpManifestPath bccpManifest() {
        if (_bccpManifest == null)
            _bccpManifest = new BccpManifestPath(this, Keys.BCCP_MANIFEST_TAG_BCCP_MANIFEST_ID_FK, null);

        return _bccpManifest;
    }

    private transient AppUserPath _appUser;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table.
     */
    public AppUserPath appUser() {
        if (_appUser == null)
            _appUser = new AppUserPath(this, Keys.BCCP_MANIFEST_TAG_CREATED_BY_FK, null);

        return _appUser;
    }

    private transient TagPath _tag;

    /**
     * Get the implicit join path to the <code>oagi.tag</code> table.
     */
    public TagPath tag() {
        if (_tag == null)
            _tag = new TagPath(this, Keys.BCCP_MANIFEST_TAG_TAG_ID_FK, null);

        return _tag;
    }

    @Override
    public BccpManifestTag as(String alias) {
        return new BccpManifestTag(DSL.name(alias), this);
    }

    @Override
    public BccpManifestTag as(Name alias) {
        return new BccpManifestTag(alias, this);
    }

    @Override
    public BccpManifestTag as(Table<?> alias) {
        return new BccpManifestTag(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public BccpManifestTag rename(String name) {
        return new BccpManifestTag(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public BccpManifestTag rename(Name name) {
        return new BccpManifestTag(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public BccpManifestTag rename(Table<?> name) {
        return new BccpManifestTag(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BccpManifestTag where(Condition condition) {
        return new BccpManifestTag(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BccpManifestTag where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BccpManifestTag where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BccpManifestTag where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BccpManifestTag where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BccpManifestTag where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BccpManifestTag where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BccpManifestTag where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BccpManifestTag whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BccpManifestTag whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
