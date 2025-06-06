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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Bbie.BbiePath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.BccBizterm.BccBiztermPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.BbieBiztermRecord;


/**
 * The bbie_bizterm table stores information about the aggregation between the
 * bbie_bizterm and BBIE. TODO: Placeholder, definition is missing.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class BbieBizterm extends TableImpl<BbieBiztermRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.bbie_bizterm</code>
     */
    public static final BbieBizterm BBIE_BIZTERM = new BbieBizterm();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BbieBiztermRecord> getRecordType() {
        return BbieBiztermRecord.class;
    }

    /**
     * The column <code>oagi.bbie_bizterm.bbie_bizterm_id</code>. An internal,
     * primary database key of an bbie_bizterm record.
     */
    public final TableField<BbieBiztermRecord, ULong> BBIE_BIZTERM_ID = createField(DSL.name("bbie_bizterm_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "An internal, primary database key of an bbie_bizterm record.");

    /**
     * The column <code>oagi.bbie_bizterm.bcc_bizterm_id</code>. An internal ID
     * of the bbie_bizterm record.
     */
    public final TableField<BbieBiztermRecord, ULong> BCC_BIZTERM_ID = createField(DSL.name("bcc_bizterm_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "An internal ID of the bbie_bizterm record.");

    /**
     * The column <code>oagi.bbie_bizterm.bbie_id</code>. An internal ID of the
     * associated BBIE
     */
    public final TableField<BbieBiztermRecord, ULong> BBIE_ID = createField(DSL.name("bbie_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "An internal ID of the associated BBIE");

    /**
     * The column <code>oagi.bbie_bizterm.primary_indicator</code>. The
     * indicator shows if the business term is primary for the assigned BBIE.
     */
    public final TableField<BbieBiztermRecord, Byte> PRIMARY_INDICATOR = createField(DSL.name("primary_indicator"), SQLDataType.TINYINT.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.TINYINT)), this, "The indicator shows if the business term is primary for the assigned BBIE.");

    /**
     * The column <code>oagi.bbie_bizterm.type_code</code>. The type code of the
     * assignment.
     */
    public final TableField<BbieBiztermRecord, String> TYPE_CODE = createField(DSL.name("type_code"), SQLDataType.CHAR(30).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CHAR)), this, "The type code of the assignment.");

    /**
     * The column <code>oagi.bbie_bizterm.created_by</code>. A foreign key
     * referring to the user who creates the bbie_bizterm record. The creator of
     * the asbie_bizterm is also its owner by default.
     */
    public final TableField<BbieBiztermRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key referring to the user who creates the bbie_bizterm record. The creator of the asbie_bizterm is also its owner by default.");

    /**
     * The column <code>oagi.bbie_bizterm.last_updated_by</code>. A foreign key
     * referring to the last user who has updated the bbie_bizterm record. This
     * may be the user who is in the same group as the creator.
     */
    public final TableField<BbieBiztermRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key referring to the last user who has updated the bbie_bizterm record. This may be the user who is in the same group as the creator.");

    /**
     * The column <code>oagi.bbie_bizterm.creation_timestamp</code>. Timestamp
     * when the bbie_bizterm record was first created.
     */
    public final TableField<BbieBiztermRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "Timestamp when the bbie_bizterm record was first created.");

    /**
     * The column <code>oagi.bbie_bizterm.last_update_timestamp</code>. The
     * timestamp when the bbie_bizterm was last updated.
     */
    public final TableField<BbieBiztermRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the bbie_bizterm was last updated.");

    private BbieBizterm(Name alias, Table<BbieBiztermRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private BbieBizterm(Name alias, Table<BbieBiztermRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment("The bbie_bizterm table stores information about the aggregation between the bbie_bizterm and BBIE. TODO: Placeholder, definition is missing."), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.bbie_bizterm</code> table reference
     */
    public BbieBizterm(String alias) {
        this(DSL.name(alias), BBIE_BIZTERM);
    }

    /**
     * Create an aliased <code>oagi.bbie_bizterm</code> table reference
     */
    public BbieBizterm(Name alias) {
        this(alias, BBIE_BIZTERM);
    }

    /**
     * Create a <code>oagi.bbie_bizterm</code> table reference
     */
    public BbieBizterm() {
        this(DSL.name("bbie_bizterm"), null);
    }

    public <O extends Record> BbieBizterm(Table<O> path, ForeignKey<O, BbieBiztermRecord> childPath, InverseForeignKey<O, BbieBiztermRecord> parentPath) {
        super(path, childPath, parentPath, BBIE_BIZTERM);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class BbieBiztermPath extends BbieBizterm implements Path<BbieBiztermRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> BbieBiztermPath(Table<O> path, ForeignKey<O, BbieBiztermRecord> childPath, InverseForeignKey<O, BbieBiztermRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private BbieBiztermPath(Name alias, Table<BbieBiztermRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public BbieBiztermPath as(String alias) {
            return new BbieBiztermPath(DSL.name(alias), this);
        }

        @Override
        public BbieBiztermPath as(Name alias) {
            return new BbieBiztermPath(alias, this);
        }

        @Override
        public BbieBiztermPath as(Table<?> alias) {
            return new BbieBiztermPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.BBIE_BIZTERM_ASBIE_BIZTERM_ASBIE_FK);
    }

    @Override
    public Identity<BbieBiztermRecord, ULong> getIdentity() {
        return (Identity<BbieBiztermRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<BbieBiztermRecord> getPrimaryKey() {
        return Keys.KEY_BBIE_BIZTERM_PRIMARY;
    }

    @Override
    public List<ForeignKey<BbieBiztermRecord, ?>> getReferences() {
        return Arrays.asList(Keys.BBIE_BIZTERM_BBIE_FK, Keys.BBIE_BIZTERM_BCC_BIZTERM_FK);
    }

    private transient BbiePath _bbie;

    /**
     * Get the implicit join path to the <code>oagi.bbie</code> table.
     */
    public BbiePath bbie() {
        if (_bbie == null)
            _bbie = new BbiePath(this, Keys.BBIE_BIZTERM_BBIE_FK, null);

        return _bbie;
    }

    private transient BccBiztermPath _bccBizterm;

    /**
     * Get the implicit join path to the <code>oagi.bcc_bizterm</code> table.
     */
    public BccBiztermPath bccBizterm() {
        if (_bccBizterm == null)
            _bccBizterm = new BccBiztermPath(this, Keys.BBIE_BIZTERM_BCC_BIZTERM_FK, null);

        return _bccBizterm;
    }

    @Override
    public BbieBizterm as(String alias) {
        return new BbieBizterm(DSL.name(alias), this);
    }

    @Override
    public BbieBizterm as(Name alias) {
        return new BbieBizterm(alias, this);
    }

    @Override
    public BbieBizterm as(Table<?> alias) {
        return new BbieBizterm(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public BbieBizterm rename(String name) {
        return new BbieBizterm(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public BbieBizterm rename(Name name) {
        return new BbieBizterm(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public BbieBizterm rename(Table<?> name) {
        return new BbieBizterm(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BbieBizterm where(Condition condition) {
        return new BbieBizterm(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BbieBizterm where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BbieBizterm where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BbieBizterm where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BbieBizterm where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BbieBizterm where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BbieBizterm where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BbieBizterm where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BbieBizterm whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BbieBizterm whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
