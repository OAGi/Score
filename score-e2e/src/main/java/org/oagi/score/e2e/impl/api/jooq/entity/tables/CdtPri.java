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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.CdtAwdPri.CdtAwdPriPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.CdtScAwdPri.CdtScAwdPriPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.CdtPriRecord;


/**
 * This table stores the CDT primitives.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CdtPri extends TableImpl<CdtPriRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.cdt_pri</code>
     */
    public static final CdtPri CDT_PRI = new CdtPri();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<CdtPriRecord> getRecordType() {
        return CdtPriRecord.class;
    }

    /**
     * The column <code>oagi.cdt_pri.cdt_pri_id</code>. Internal, primary
     * database key.
     */
    public final TableField<CdtPriRecord, ULong> CDT_PRI_ID = createField(DSL.name("cdt_pri_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "Internal, primary database key.");

    /**
     * The column <code>oagi.cdt_pri.name</code>. Name of the CDT primitive per
     * the CCTS datatype catalog, e.g., Decimal.
     */
    public final TableField<CdtPriRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(45).nullable(false), this, "Name of the CDT primitive per the CCTS datatype catalog, e.g., Decimal.");

    private CdtPri(Name alias, Table<CdtPriRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private CdtPri(Name alias, Table<CdtPriRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment("This table stores the CDT primitives."), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.cdt_pri</code> table reference
     */
    public CdtPri(String alias) {
        this(DSL.name(alias), CDT_PRI);
    }

    /**
     * Create an aliased <code>oagi.cdt_pri</code> table reference
     */
    public CdtPri(Name alias) {
        this(alias, CDT_PRI);
    }

    /**
     * Create a <code>oagi.cdt_pri</code> table reference
     */
    public CdtPri() {
        this(DSL.name("cdt_pri"), null);
    }

    public <O extends Record> CdtPri(Table<O> path, ForeignKey<O, CdtPriRecord> childPath, InverseForeignKey<O, CdtPriRecord> parentPath) {
        super(path, childPath, parentPath, CDT_PRI);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class CdtPriPath extends CdtPri implements Path<CdtPriRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> CdtPriPath(Table<O> path, ForeignKey<O, CdtPriRecord> childPath, InverseForeignKey<O, CdtPriRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private CdtPriPath(Name alias, Table<CdtPriRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public CdtPriPath as(String alias) {
            return new CdtPriPath(DSL.name(alias), this);
        }

        @Override
        public CdtPriPath as(Name alias) {
            return new CdtPriPath(alias, this);
        }

        @Override
        public CdtPriPath as(Table<?> alias) {
            return new CdtPriPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<CdtPriRecord, ULong> getIdentity() {
        return (Identity<CdtPriRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<CdtPriRecord> getPrimaryKey() {
        return Keys.KEY_CDT_PRI_PRIMARY;
    }

    @Override
    public List<UniqueKey<CdtPriRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.KEY_CDT_PRI_CDT_PRI_UK1);
    }

    private transient CdtAwdPriPath _cdtAwdPri;

    /**
     * Get the implicit to-many join path to the <code>oagi.cdt_awd_pri</code>
     * table
     */
    public CdtAwdPriPath cdtAwdPri() {
        if (_cdtAwdPri == null)
            _cdtAwdPri = new CdtAwdPriPath(this, null, Keys.CDT_AWD_PRI_CDT_PRI_ID_FK.getInverseKey());

        return _cdtAwdPri;
    }

    private transient CdtScAwdPriPath _cdtScAwdPri;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.cdt_sc_awd_pri</code> table
     */
    public CdtScAwdPriPath cdtScAwdPri() {
        if (_cdtScAwdPri == null)
            _cdtScAwdPri = new CdtScAwdPriPath(this, null, Keys.CDT_SC_AWD_PRI_CDT_PRI_ID_FK.getInverseKey());

        return _cdtScAwdPri;
    }

    @Override
    public CdtPri as(String alias) {
        return new CdtPri(DSL.name(alias), this);
    }

    @Override
    public CdtPri as(Name alias) {
        return new CdtPri(alias, this);
    }

    @Override
    public CdtPri as(Table<?> alias) {
        return new CdtPri(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public CdtPri rename(String name) {
        return new CdtPri(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public CdtPri rename(Name name) {
        return new CdtPri(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public CdtPri rename(Table<?> name) {
        return new CdtPri(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public CdtPri where(Condition condition) {
        return new CdtPri(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public CdtPri where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public CdtPri where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public CdtPri where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public CdtPri where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public CdtPri where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public CdtPri where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public CdtPri where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public CdtPri whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public CdtPri whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
