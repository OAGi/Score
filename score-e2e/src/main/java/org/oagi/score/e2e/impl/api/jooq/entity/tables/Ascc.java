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
import org.jooq.Identity;
import org.jooq.Index;
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
import org.oagi.score.e2e.impl.api.jooq.entity.Indexes;
import org.oagi.score.e2e.impl.api.jooq.entity.Keys;
import org.oagi.score.e2e.impl.api.jooq.entity.Oagi;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Acc.AccPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AppUser.AppUserPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Ascc.AsccPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AsccBizterm.AsccBiztermPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AsccManifest.AsccManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Asccp.AsccpPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.AsccRecord;


/**
 * An ASCC represents a relationship/association between two ACCs through an
 * ASCCP. 
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Ascc extends TableImpl<AsccRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.ascc</code>
     */
    public static final Ascc ASCC = new Ascc();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AsccRecord> getRecordType() {
        return AsccRecord.class;
    }

    /**
     * The column <code>oagi.ascc.ascc_id</code>. An internal, primary database
     * key of an ASCC.
     */
    public final TableField<AsccRecord, ULong> ASCC_ID = createField(DSL.name("ascc_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "An internal, primary database key of an ASCC.");

    /**
     * The column <code>oagi.ascc.guid</code>. A globally unique identifier
     * (GUID).
     */
    public final TableField<AsccRecord, String> GUID = createField(DSL.name("guid"), SQLDataType.CHAR(32).nullable(false), this, "A globally unique identifier (GUID).");

    /**
     * The column <code>oagi.ascc.cardinality_min</code>. Minimum occurrence of
     * the TO_ASCCP_ID. The valid values are non-negative integer.
     */
    public final TableField<AsccRecord, Integer> CARDINALITY_MIN = createField(DSL.name("cardinality_min"), SQLDataType.INTEGER.nullable(false), this, "Minimum occurrence of the TO_ASCCP_ID. The valid values are non-negative integer.");

    /**
     * The column <code>oagi.ascc.cardinality_max</code>. Maximum cardinality of
     * the TO_ASCCP_ID. A valid value is integer -1 and up. Specifically, -1
     * means unbounded. 0 means prohibited or not to use.
     */
    public final TableField<AsccRecord, Integer> CARDINALITY_MAX = createField(DSL.name("cardinality_max"), SQLDataType.INTEGER.nullable(false), this, "Maximum cardinality of the TO_ASCCP_ID. A valid value is integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.");

    /**
     * The column <code>oagi.ascc.seq_key</code>. @deprecated since 2.0.0. This
     * indicates the order of the associations among other siblings. A valid
     * value is positive integer. The SEQ_KEY at the CC side is localized. In
     * other words, if an ACC is based on another ACC, SEQ_KEY of ASCCs or BCCs
     * of the former ACC starts at 1 again.
     */
    public final TableField<AsccRecord, Integer> SEQ_KEY = createField(DSL.name("seq_key"), SQLDataType.INTEGER.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.INTEGER)), this, "@deprecated since 2.0.0. This indicates the order of the associations among other siblings. A valid value is positive integer. The SEQ_KEY at the CC side is localized. In other words, if an ACC is based on another ACC, SEQ_KEY of ASCCs or BCCs of the former ACC starts at 1 again.");

    /**
     * The column <code>oagi.ascc.from_acc_id</code>. FROM_ACC_ID is a foreign
     * key pointing to an ACC record. It is basically pointing to a parent data
     * element (type) of the TO_ASCCP_ID.
     */
    public final TableField<AsccRecord, ULong> FROM_ACC_ID = createField(DSL.name("from_acc_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "FROM_ACC_ID is a foreign key pointing to an ACC record. It is basically pointing to a parent data element (type) of the TO_ASCCP_ID.");

    /**
     * The column <code>oagi.ascc.to_asccp_id</code>. TO_ASCCP_ID is a foreign
     * key to an ASCCP table record. It is basically pointing to a child data
     * element of the FROM_ACC_ID. 
     */
    public final TableField<AsccRecord, ULong> TO_ASCCP_ID = createField(DSL.name("to_asccp_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "TO_ASCCP_ID is a foreign key to an ASCCP table record. It is basically pointing to a child data element of the FROM_ACC_ID. ");

    /**
     * The column <code>oagi.ascc.definition</code>. This is a documentation or
     * description of the ASCC. Since ASCC is business context independent, this
     * is a business context independent description of the ASCC. Since there
     * are definitions also in the ASCCP (as referenced by the TO_ASCCP_ID
     * column) and the ACC under that ASCCP, definition in the ASCC is a
     * specific description about the relationship between the ACC (as in
     * FROM_ACC_ID) and the ASCCP.
     */
    public final TableField<AsccRecord, String> DEFINITION = createField(DSL.name("definition"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "This is a documentation or description of the ASCC. Since ASCC is business context independent, this is a business context independent description of the ASCC. Since there are definitions also in the ASCCP (as referenced by the TO_ASCCP_ID column) and the ACC under that ASCCP, definition in the ASCC is a specific description about the relationship between the ACC (as in FROM_ACC_ID) and the ASCCP.");

    /**
     * The column <code>oagi.ascc.definition_source</code>. This is typically a
     * URL identifying the source of the DEFINITION column.
     */
    public final TableField<AsccRecord, String> DEFINITION_SOURCE = createField(DSL.name("definition_source"), SQLDataType.VARCHAR(100).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "This is typically a URL identifying the source of the DEFINITION column.");

    /**
     * The column <code>oagi.ascc.is_deprecated</code>. Indicates whether the CC
     * is deprecated and should not be reused (i.e., no new reference to this
     * record should be created).
     */
    public final TableField<AsccRecord, Byte> IS_DEPRECATED = createField(DSL.name("is_deprecated"), SQLDataType.TINYINT.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.TINYINT)), this, "Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).");

    /**
     * The column <code>oagi.ascc.replacement_ascc_id</code>. This refers to a
     * replacement if the record is deprecated.
     */
    public final TableField<AsccRecord, ULong> REPLACEMENT_ASCC_ID = createField(DSL.name("replacement_ascc_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "This refers to a replacement if the record is deprecated.");

    /**
     * The column <code>oagi.ascc.created_by</code>. A foreign key to the
     * APP_USER table referring to the user who creates the entity.
     * 
     * This column never change between the history and the current record for a
     * given revision. The history record should have the same value as that of
     * its current record.
     */
    public final TableField<AsccRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key to the APP_USER table referring to the user who creates the entity.\n\nThis column never change between the history and the current record for a given revision. The history record should have the same value as that of its current record.");

    /**
     * The column <code>oagi.ascc.owner_user_id</code>. Foreign key to the
     * APP_USER table. This is the user who owns the entity, is allowed to edit
     * the entity, and who can transfer the ownership to another user.
     * 
     * The ownership can change throughout the history, but undoing shouldn't
     * rollback the ownership. 
     */
    public final TableField<AsccRecord, ULong> OWNER_USER_ID = createField(DSL.name("owner_user_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn't rollback the ownership. ");

    /**
     * The column <code>oagi.ascc.last_updated_by</code>. A foreign key to the
     * APP_USER table referring to the last user who has updated the record. 
     * 
     * In the history record, this should always be the user who is editing the
     * entity (perhaps except when the ownership has just been changed).
     */
    public final TableField<AsccRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key to the APP_USER table referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).");

    /**
     * The column <code>oagi.ascc.creation_timestamp</code>. Timestamp when the
     * revision of the ASCC was created. 
     * 
     * This never change for a revision.
     */
    public final TableField<AsccRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "Timestamp when the revision of the ASCC was created. \n\nThis never change for a revision.");

    /**
     * The column <code>oagi.ascc.last_update_timestamp</code>. The timestamp
     * when the record was last updated.
     * 
     * The value of this column in the latest history record should be the same
     * as that of the current record. This column keeps the record of when the
     * change has occurred.
     */
    public final TableField<AsccRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the change has occurred.");

    /**
     * The column <code>oagi.ascc.state</code>. Deleted, WIP, Draft, QA,
     * Candidate, Production, Release Draft, Published. This the revision life
     * cycle state of the BCC.
     * 
     * State change can't be undone. But the history record can still keep the
     * records of when the state was changed.
     */
    public final TableField<AsccRecord, String> STATE = createField(DSL.name("state"), SQLDataType.VARCHAR(20).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published. This the revision life cycle state of the BCC.\n\nState change can't be undone. But the history record can still keep the records of when the state was changed.");

    /**
     * The column <code>oagi.ascc.prev_ascc_id</code>. A self-foreign key to
     * indicate the previous history record.
     */
    public final TableField<AsccRecord, ULong> PREV_ASCC_ID = createField(DSL.name("prev_ascc_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "A self-foreign key to indicate the previous history record.");

    /**
     * The column <code>oagi.ascc.next_ascc_id</code>. A self-foreign key to
     * indicate the next history record.
     */
    public final TableField<AsccRecord, ULong> NEXT_ASCC_ID = createField(DSL.name("next_ascc_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "A self-foreign key to indicate the next history record.");

    private Ascc(Name alias, Table<AsccRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Ascc(Name alias, Table<AsccRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment("An ASCC represents a relationship/association between two ACCs through an ASCCP. "), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.ascc</code> table reference
     */
    public Ascc(String alias) {
        this(DSL.name(alias), ASCC);
    }

    /**
     * Create an aliased <code>oagi.ascc</code> table reference
     */
    public Ascc(Name alias) {
        this(alias, ASCC);
    }

    /**
     * Create a <code>oagi.ascc</code> table reference
     */
    public Ascc() {
        this(DSL.name("ascc"), null);
    }

    public <O extends Record> Ascc(Table<O> path, ForeignKey<O, AsccRecord> childPath, InverseForeignKey<O, AsccRecord> parentPath) {
        super(path, childPath, parentPath, ASCC);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class AsccPath extends Ascc implements Path<AsccRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> AsccPath(Table<O> path, ForeignKey<O, AsccRecord> childPath, InverseForeignKey<O, AsccRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private AsccPath(Name alias, Table<AsccRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public AsccPath as(String alias) {
            return new AsccPath(DSL.name(alias), this);
        }

        @Override
        public AsccPath as(Name alias) {
            return new AsccPath(alias, this);
        }

        @Override
        public AsccPath as(Table<?> alias) {
            return new AsccPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.ASCC_ASCC_GUID_IDX, Indexes.ASCC_ASCC_LAST_UPDATE_TIMESTAMP_DESC_IDX);
    }

    @Override
    public Identity<AsccRecord, ULong> getIdentity() {
        return (Identity<AsccRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<AsccRecord> getPrimaryKey() {
        return Keys.KEY_ASCC_PRIMARY;
    }

    @Override
    public List<ForeignKey<AsccRecord, ?>> getReferences() {
        return Arrays.asList(Keys.ASCC_CREATED_BY_FK, Keys.ASCC_FROM_ACC_ID_FK, Keys.ASCC_LAST_UPDATED_BY_FK, Keys.ASCC_NEXT_ASCC_ID_FK, Keys.ASCC_OWNER_USER_ID_FK, Keys.ASCC_PREV_ASCC_ID_FK, Keys.ASCC_REPLACEMENT_ASCC_ID_FK, Keys.ASCC_TO_ASCCP_ID_FK);
    }

    private transient AppUserPath _asccCreatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>ascc_created_by_fk</code> key.
     */
    public AppUserPath asccCreatedByFk() {
        if (_asccCreatedByFk == null)
            _asccCreatedByFk = new AppUserPath(this, Keys.ASCC_CREATED_BY_FK, null);

        return _asccCreatedByFk;
    }

    private transient AccPath _acc;

    /**
     * Get the implicit join path to the <code>oagi.acc</code> table.
     */
    public AccPath acc() {
        if (_acc == null)
            _acc = new AccPath(this, Keys.ASCC_FROM_ACC_ID_FK, null);

        return _acc;
    }

    private transient AppUserPath _asccLastUpdatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>ascc_last_updated_by_fk</code> key.
     */
    public AppUserPath asccLastUpdatedByFk() {
        if (_asccLastUpdatedByFk == null)
            _asccLastUpdatedByFk = new AppUserPath(this, Keys.ASCC_LAST_UPDATED_BY_FK, null);

        return _asccLastUpdatedByFk;
    }

    private transient AsccPath _asccNextAsccIdFk;

    /**
     * Get the implicit join path to the <code>oagi.ascc</code> table, via the
     * <code>ascc_next_ascc_id_fk</code> key.
     */
    public AsccPath asccNextAsccIdFk() {
        if (_asccNextAsccIdFk == null)
            _asccNextAsccIdFk = new AsccPath(this, Keys.ASCC_NEXT_ASCC_ID_FK, null);

        return _asccNextAsccIdFk;
    }

    private transient AppUserPath _asccOwnerUserIdFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>ascc_owner_user_id_fk</code> key.
     */
    public AppUserPath asccOwnerUserIdFk() {
        if (_asccOwnerUserIdFk == null)
            _asccOwnerUserIdFk = new AppUserPath(this, Keys.ASCC_OWNER_USER_ID_FK, null);

        return _asccOwnerUserIdFk;
    }

    private transient AsccPath _asccPrevAsccIdFk;

    /**
     * Get the implicit join path to the <code>oagi.ascc</code> table, via the
     * <code>ascc_prev_ascc_id_fk</code> key.
     */
    public AsccPath asccPrevAsccIdFk() {
        if (_asccPrevAsccIdFk == null)
            _asccPrevAsccIdFk = new AsccPath(this, Keys.ASCC_PREV_ASCC_ID_FK, null);

        return _asccPrevAsccIdFk;
    }

    private transient AsccPath _asccReplacementAsccIdFk;

    /**
     * Get the implicit join path to the <code>oagi.ascc</code> table, via the
     * <code>ascc_replacement_ascc_id_fk</code> key.
     */
    public AsccPath asccReplacementAsccIdFk() {
        if (_asccReplacementAsccIdFk == null)
            _asccReplacementAsccIdFk = new AsccPath(this, Keys.ASCC_REPLACEMENT_ASCC_ID_FK, null);

        return _asccReplacementAsccIdFk;
    }

    private transient AsccpPath _asccp;

    /**
     * Get the implicit join path to the <code>oagi.asccp</code> table.
     */
    public AsccpPath asccp() {
        if (_asccp == null)
            _asccp = new AsccpPath(this, Keys.ASCC_TO_ASCCP_ID_FK, null);

        return _asccp;
    }

    private transient AsccBiztermPath _asccBizterm;

    /**
     * Get the implicit to-many join path to the <code>oagi.ascc_bizterm</code>
     * table
     */
    public AsccBiztermPath asccBizterm() {
        if (_asccBizterm == null)
            _asccBizterm = new AsccBiztermPath(this, null, Keys.ASCC_BIZTERM_ASCC_FK.getInverseKey());

        return _asccBizterm;
    }

    private transient AsccManifestPath _asccManifest;

    /**
     * Get the implicit to-many join path to the <code>oagi.ascc_manifest</code>
     * table
     */
    public AsccManifestPath asccManifest() {
        if (_asccManifest == null)
            _asccManifest = new AsccManifestPath(this, null, Keys.ASCC_MANIFEST_ASCC_ID_FK.getInverseKey());

        return _asccManifest;
    }

    @Override
    public Ascc as(String alias) {
        return new Ascc(DSL.name(alias), this);
    }

    @Override
    public Ascc as(Name alias) {
        return new Ascc(alias, this);
    }

    @Override
    public Ascc as(Table<?> alias) {
        return new Ascc(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Ascc rename(String name) {
        return new Ascc(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Ascc rename(Name name) {
        return new Ascc(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Ascc rename(Table<?> name) {
        return new Ascc(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Ascc where(Condition condition) {
        return new Ascc(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Ascc where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Ascc where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Ascc where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Ascc where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Ascc where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Ascc where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Ascc where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Ascc whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Ascc whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
