/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.repo.api.impl.jooq.entity.tables;


import java.time.LocalDateTime;
import java.util.Collection;

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
import org.oagi.score.repo.api.impl.jooq.entity.Keys;
import org.oagi.score.repo.api.impl.jooq.entity.Oagi;
import org.oagi.score.repo.api.impl.jooq.entity.tables.AsccBizterm.AsccBiztermPath;
import org.oagi.score.repo.api.impl.jooq.entity.tables.BccBizterm.BccBiztermPath;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BusinessTermRecord;


/**
 * The Business Term table stores information about the business term, which is
 * usually associated to BIE or CC. TODO: Placeeholder, definition is missing.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BusinessTerm extends TableImpl<BusinessTermRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.business_term</code>
     */
    public static final BusinessTerm BUSINESS_TERM = new BusinessTerm();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BusinessTermRecord> getRecordType() {
        return BusinessTermRecord.class;
    }

    /**
     * The column <code>oagi.business_term.business_term_id</code>. A internal,
     * primary database key of an Business term.
     */
    public final TableField<BusinessTermRecord, ULong> BUSINESS_TERM_ID = createField(DSL.name("business_term_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "A internal, primary database key of an Business term.");

    /**
     * The column <code>oagi.business_term.guid</code>. A globally unique
     * identifier (GUID).
     */
    public final TableField<BusinessTermRecord, String> GUID = createField(DSL.name("guid"), SQLDataType.CHAR(32).nullable(false), this, "A globally unique identifier (GUID).");

    /**
     * The column <code>oagi.business_term.business_term</code>. A main name of
     * the business term
     */
    public final TableField<BusinessTermRecord, String> BUSINESS_TERM_ = createField(DSL.name("business_term"), SQLDataType.VARCHAR(255).nullable(false), this, "A main name of the business term");

    /**
     * The column <code>oagi.business_term.definition</code>. Definition of the
     * business term.
     */
    public final TableField<BusinessTermRecord, String> DEFINITION = createField(DSL.name("definition"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "Definition of the business term.");

    /**
     * The column <code>oagi.business_term.created_by</code>. A foreign key
     * referring to the user who creates the business term. The creator of the
     * business term is also its owner by default.
     */
    public final TableField<BusinessTermRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key referring to the user who creates the business term. The creator of the business term is also its owner by default.");

    /**
     * The column <code>oagi.business_term.last_updated_by</code>. A foreign key
     * referring to the last user who has updated the business term record. This
     * may be the user who is in the same group as the creator.
     */
    public final TableField<BusinessTermRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key referring to the last user who has updated the business term record. This may be the user who is in the same group as the creator.");

    /**
     * The column <code>oagi.business_term.creation_timestamp</code>. Timestamp
     * when the business term record was first created.
     */
    public final TableField<BusinessTermRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "Timestamp when the business term record was first created.");

    /**
     * The column <code>oagi.business_term.last_update_timestamp</code>. The
     * timestamp when the business term was last updated.
     */
    public final TableField<BusinessTermRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the business term was last updated.");

    /**
     * The column <code>oagi.business_term.external_ref_uri</code>. TODO:
     * Definition is missing.
     */
    public final TableField<BusinessTermRecord, String> EXTERNAL_REF_URI = createField(DSL.name("external_ref_uri"), SQLDataType.CLOB.nullable(false), this, "TODO: Definition is missing.");

    /**
     * The column <code>oagi.business_term.external_ref_id</code>. TODO:
     * Definition is missing.
     */
    public final TableField<BusinessTermRecord, String> EXTERNAL_REF_ID = createField(DSL.name("external_ref_id"), SQLDataType.VARCHAR(100).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "TODO: Definition is missing.");

    /**
     * The column <code>oagi.business_term.comment</code>. Comment of the
     * business term.
     */
    public final TableField<BusinessTermRecord, String> COMMENT = createField(DSL.name("comment"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "Comment of the business term.");

    private BusinessTerm(Name alias, Table<BusinessTermRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private BusinessTerm(Name alias, Table<BusinessTermRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment("The Business Term table stores information about the business term, which is usually associated to BIE or CC. TODO: Placeeholder, definition is missing."), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.business_term</code> table reference
     */
    public BusinessTerm(String alias) {
        this(DSL.name(alias), BUSINESS_TERM);
    }

    /**
     * Create an aliased <code>oagi.business_term</code> table reference
     */
    public BusinessTerm(Name alias) {
        this(alias, BUSINESS_TERM);
    }

    /**
     * Create a <code>oagi.business_term</code> table reference
     */
    public BusinessTerm() {
        this(DSL.name("business_term"), null);
    }

    public <O extends Record> BusinessTerm(Table<O> path, ForeignKey<O, BusinessTermRecord> childPath, InverseForeignKey<O, BusinessTermRecord> parentPath) {
        super(path, childPath, parentPath, BUSINESS_TERM);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class BusinessTermPath extends BusinessTerm implements Path<BusinessTermRecord> {
        public <O extends Record> BusinessTermPath(Table<O> path, ForeignKey<O, BusinessTermRecord> childPath, InverseForeignKey<O, BusinessTermRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private BusinessTermPath(Name alias, Table<BusinessTermRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public BusinessTermPath as(String alias) {
            return new BusinessTermPath(DSL.name(alias), this);
        }

        @Override
        public BusinessTermPath as(Name alias) {
            return new BusinessTermPath(alias, this);
        }

        @Override
        public BusinessTermPath as(Table<?> alias) {
            return new BusinessTermPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<BusinessTermRecord, ULong> getIdentity() {
        return (Identity<BusinessTermRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<BusinessTermRecord> getPrimaryKey() {
        return Keys.KEY_BUSINESS_TERM_PRIMARY;
    }

    private transient AsccBiztermPath _asccBizterm;

    /**
     * Get the implicit to-many join path to the <code>oagi.ascc_bizterm</code>
     * table
     */
    public AsccBiztermPath asccBizterm() {
        if (_asccBizterm == null)
            _asccBizterm = new AsccBiztermPath(this, null, Keys.ASCC_BIZTERM_BUSINESS_TERM_FK.getInverseKey());

        return _asccBizterm;
    }

    private transient BccBiztermPath _bccBizterm;

    /**
     * Get the implicit to-many join path to the <code>oagi.bcc_bizterm</code>
     * table
     */
    public BccBiztermPath bccBizterm() {
        if (_bccBizterm == null)
            _bccBizterm = new BccBiztermPath(this, null, Keys.BCC_BIZTERM_BUSINESS_TERM_FK.getInverseKey());

        return _bccBizterm;
    }

    @Override
    public BusinessTerm as(String alias) {
        return new BusinessTerm(DSL.name(alias), this);
    }

    @Override
    public BusinessTerm as(Name alias) {
        return new BusinessTerm(alias, this);
    }

    @Override
    public BusinessTerm as(Table<?> alias) {
        return new BusinessTerm(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public BusinessTerm rename(String name) {
        return new BusinessTerm(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public BusinessTerm rename(Name name) {
        return new BusinessTerm(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public BusinessTerm rename(Table<?> name) {
        return new BusinessTerm(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BusinessTerm where(Condition condition) {
        return new BusinessTerm(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BusinessTerm where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BusinessTerm where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BusinessTerm where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BusinessTerm where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BusinessTerm where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BusinessTerm where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BusinessTerm where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BusinessTerm whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BusinessTerm whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
