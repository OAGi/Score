/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables;


import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.Keys;
import org.oagi.score.e2e.impl.api.jooq.entity.Oagi;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.UsageRuleRecord;

import java.util.function.Function;


/**
 * This table captures a usage rule information. A usage rule may be expressed
 * in multiple expressions. Each expression is captured in the
 * USAGE_RULE_EXPRESSION table. To capture a description of a usage rule, create
 * a usage rule expression with the unstructured constraint type.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class UsageRule extends TableImpl<UsageRuleRecord> {

    /**
     * The reference instance of <code>oagi.usage_rule</code>
     */
    public static final UsageRule USAGE_RULE = new UsageRule();
    private static final long serialVersionUID = 1L;
    /**
     * The column <code>oagi.usage_rule.usage_rule_id</code>. Primary key of the
     * usage rule.
     */
    public final TableField<UsageRuleRecord, ULong> USAGE_RULE_ID = createField(DSL.name("usage_rule_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "Primary key of the usage rule.");
    /**
     * The column <code>oagi.usage_rule.name</code>. Short nmenomic name of the
     * usage rule.
     */
    public final TableField<UsageRuleRecord, String> NAME = createField(DSL.name("name"), SQLDataType.CLOB, this, "Short nmenomic name of the usage rule.");
    /**
     * The column <code>oagi.usage_rule.condition_type</code>. Condition type
     * according to the CC specification. It is a value list column. 0 =
     * pre-condition, 1 = post-condition, 2 = invariant.
     */
    public final TableField<UsageRuleRecord, Integer> CONDITION_TYPE = createField(DSL.name("condition_type"), SQLDataType.INTEGER.nullable(false), this, "Condition type according to the CC specification. It is a value list column. 0 = pre-condition, 1 = post-condition, 2 = invariant.");

    private UsageRule(Name alias, Table<UsageRuleRecord> aliased) {
        this(alias, aliased, null);
    }

    private UsageRule(Name alias, Table<UsageRuleRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("This table captures a usage rule information. A usage rule may be expressed in multiple expressions. Each expression is captured in the USAGE_RULE_EXPRESSION table. To capture a description of a usage rule, create a usage rule expression with the unstructured constraint type."), TableOptions.table());
    }

    /**
     * Create an aliased <code>oagi.usage_rule</code> table reference
     */
    public UsageRule(String alias) {
        this(DSL.name(alias), USAGE_RULE);
    }

    /**
     * Create an aliased <code>oagi.usage_rule</code> table reference
     */
    public UsageRule(Name alias) {
        this(alias, USAGE_RULE);
    }

    /**
     * Create a <code>oagi.usage_rule</code> table reference
     */
    public UsageRule() {
        this(DSL.name("usage_rule"), null);
    }

    public <O extends Record> UsageRule(Table<O> child, ForeignKey<O, UsageRuleRecord> key) {
        super(child, key, USAGE_RULE);
    }

    /**
     * The class holding records for this type
     */
    @Override
    public Class<UsageRuleRecord> getRecordType() {
        return UsageRuleRecord.class;
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<UsageRuleRecord, ULong> getIdentity() {
        return (Identity<UsageRuleRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<UsageRuleRecord> getPrimaryKey() {
        return Keys.KEY_USAGE_RULE_PRIMARY;
    }

    @Override
    public UsageRule as(String alias) {
        return new UsageRule(DSL.name(alias), this);
    }

    @Override
    public UsageRule as(Name alias) {
        return new UsageRule(alias, this);
    }

    @Override
    public UsageRule as(Table<?> alias) {
        return new UsageRule(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public UsageRule rename(String name) {
        return new UsageRule(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public UsageRule rename(Name name) {
        return new UsageRule(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public UsageRule rename(Table<?> name) {
        return new UsageRule(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<ULong, String, Integer> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function3<? super ULong, ? super String, ? super Integer, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function3<? super ULong, ? super String, ? super Integer, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
