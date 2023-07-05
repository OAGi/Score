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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.BusinessTermRecord;

import java.time.LocalDateTime;
import java.util.function.Function;


/**
 * The Business Term table stores information about the business term, which is
 * usually associated to BIE or CC. TODO: Placeeholder, definition is missing.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class BusinessTerm extends TableImpl<BusinessTermRecord> {

    /**
     * The reference instance of <code>oagi.business_term</code>
     */
    public static final BusinessTerm BUSINESS_TERM = new BusinessTerm();
    private static final long serialVersionUID = 1L;
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
    public final TableField<BusinessTermRecord, String> BUSINESS_TERM_ = createField(DSL.name("business_term"), SQLDataType.VARCHAR(200).nullable(false), this, "A main name of the business term");
    /**
     * The column <code>oagi.business_term.definition</code>. Definition of the
     * business term.
     */
    public final TableField<BusinessTermRecord, String> DEFINITION = createField(DSL.name("definition"), SQLDataType.CLOB, this, "Definition of the business term.");
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
    public final TableField<BusinessTermRecord, String> EXTERNAL_REF_ID = createField(DSL.name("external_ref_id"), SQLDataType.VARCHAR(100), this, "TODO: Definition is missing.");
    /**
     * The column <code>oagi.business_term.comment</code>. Comment of the
     * business term.
     */
    public final TableField<BusinessTermRecord, String> COMMENT = createField(DSL.name("comment"), SQLDataType.CLOB, this, "Comment of the business term.");

    private BusinessTerm(Name alias, Table<BusinessTermRecord> aliased) {
        this(alias, aliased, null);
    }

    private BusinessTerm(Name alias, Table<BusinessTermRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("The Business Term table stores information about the business term, which is usually associated to BIE or CC. TODO: Placeeholder, definition is missing."), TableOptions.table());
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

    public <O extends Record> BusinessTerm(Table<O> child, ForeignKey<O, BusinessTermRecord> key) {
        super(child, key, BUSINESS_TERM);
    }

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BusinessTermRecord> getRecordType() {
        return BusinessTermRecord.class;
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

    // -------------------------------------------------------------------------
    // Row11 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row11<ULong, String, String, String, ULong, ULong, LocalDateTime, LocalDateTime, String, String, String> fieldsRow() {
        return (Row11) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function11<? super ULong, ? super String, ? super String, ? super String, ? super ULong, ? super ULong, ? super LocalDateTime, ? super LocalDateTime, ? super String, ? super String, ? super String, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function11<? super ULong, ? super String, ? super String, ? super String, ? super ULong, ? super ULong, ? super LocalDateTime, ? super LocalDateTime, ? super String, ? super String, ? super String, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
