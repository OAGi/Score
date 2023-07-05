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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.BccBiztermRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


/**
 * The bcc_bizterm table stores information about the aggregation between the
 * business term and BCC. TODO: Placeholder, definition is missing.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class BccBizterm extends TableImpl<BccBiztermRecord> {

    /**
     * The reference instance of <code>oagi.bcc_bizterm</code>
     */
    public static final BccBizterm BCC_BIZTERM = new BccBizterm();
    private static final long serialVersionUID = 1L;
    /**
     * The column <code>oagi.bcc_bizterm.bcc_bizterm_id</code>. An internal,
     * primary database key of an bcc_bizterm record.
     */
    public final TableField<BccBiztermRecord, ULong> BCC_BIZTERM_ID = createField(DSL.name("bcc_bizterm_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "An internal, primary database key of an bcc_bizterm record.");
    /**
     * The column <code>oagi.bcc_bizterm.business_term_id</code>. An internal ID
     * of the associated business term
     */
    public final TableField<BccBiztermRecord, ULong> BUSINESS_TERM_ID = createField(DSL.name("business_term_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "An internal ID of the associated business term");
    /**
     * The column <code>oagi.bcc_bizterm.bcc_id</code>. An internal ID of the
     * associated BCC
     */
    public final TableField<BccBiztermRecord, ULong> BCC_ID = createField(DSL.name("bcc_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "An internal ID of the associated BCC");
    /**
     * The column <code>oagi.bcc_bizterm.created_by</code>. A foreign key
     * referring to the user who creates the bcc_bizterm record. The creator of
     * the bcc_bizterm is also its owner by default.
     */
    public final TableField<BccBiztermRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key referring to the user who creates the bcc_bizterm record. The creator of the bcc_bizterm is also its owner by default.");
    /**
     * The column <code>oagi.bcc_bizterm.last_updated_by</code>. A foreign key
     * referring to the last user who has updated the bcc_bizterm record. This
     * may be the user who is in the same group as the creator.
     */
    public final TableField<BccBiztermRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key referring to the last user who has updated the bcc_bizterm record. This may be the user who is in the same group as the creator.");
    /**
     * The column <code>oagi.bcc_bizterm.creation_timestamp</code>. Timestamp
     * when the bcc_bizterm record was first created.
     */
    public final TableField<BccBiztermRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "Timestamp when the bcc_bizterm record was first created.");
    /**
     * The column <code>oagi.bcc_bizterm.last_update_timestamp</code>. The
     * timestamp when the bcc_bizterm was last updated.
     */
    public final TableField<BccBiztermRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the bcc_bizterm was last updated.");
    private transient BusinessTerm _businessTerm;
    private transient Bcc _bcc;

    private BccBizterm(Name alias, Table<BccBiztermRecord> aliased) {
        this(alias, aliased, null);
    }

    private BccBizterm(Name alias, Table<BccBiztermRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("The bcc_bizterm table stores information about the aggregation between the business term and BCC. TODO: Placeholder, definition is missing."), TableOptions.table());
    }

    /**
     * Create an aliased <code>oagi.bcc_bizterm</code> table reference
     */
    public BccBizterm(String alias) {
        this(DSL.name(alias), BCC_BIZTERM);
    }

    /**
     * Create an aliased <code>oagi.bcc_bizterm</code> table reference
     */
    public BccBizterm(Name alias) {
        this(alias, BCC_BIZTERM);
    }

    /**
     * Create a <code>oagi.bcc_bizterm</code> table reference
     */
    public BccBizterm() {
        this(DSL.name("bcc_bizterm"), null);
    }

    public <O extends Record> BccBizterm(Table<O> child, ForeignKey<O, BccBiztermRecord> key) {
        super(child, key, BCC_BIZTERM);
    }

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BccBiztermRecord> getRecordType() {
        return BccBiztermRecord.class;
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<BccBiztermRecord, ULong> getIdentity() {
        return (Identity<BccBiztermRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<BccBiztermRecord> getPrimaryKey() {
        return Keys.KEY_BCC_BIZTERM_PRIMARY;
    }

    @Override
    public List<ForeignKey<BccBiztermRecord, ?>> getReferences() {
        return Arrays.asList(Keys.BCC_BIZTERM_BUSINESS_TERM_FK, Keys.BCC_BIZTERM_BCC_FK);
    }

    /**
     * Get the implicit join path to the <code>oagi.business_term</code> table.
     */
    public BusinessTerm businessTerm() {
        if (_businessTerm == null)
            _businessTerm = new BusinessTerm(this, Keys.BCC_BIZTERM_BUSINESS_TERM_FK);

        return _businessTerm;
    }

    /**
     * Get the implicit join path to the <code>oagi.bcc</code> table.
     */
    public Bcc bcc() {
        if (_bcc == null)
            _bcc = new Bcc(this, Keys.BCC_BIZTERM_BCC_FK);

        return _bcc;
    }

    @Override
    public BccBizterm as(String alias) {
        return new BccBizterm(DSL.name(alias), this);
    }

    @Override
    public BccBizterm as(Name alias) {
        return new BccBizterm(alias, this);
    }

    @Override
    public BccBizterm as(Table<?> alias) {
        return new BccBizterm(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public BccBizterm rename(String name) {
        return new BccBizterm(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public BccBizterm rename(Name name) {
        return new BccBizterm(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public BccBizterm rename(Table<?> name) {
        return new BccBizterm(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row7 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row7<ULong, ULong, ULong, ULong, ULong, LocalDateTime, LocalDateTime> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function7<? super ULong, ? super ULong, ? super ULong, ? super ULong, ? super ULong, ? super LocalDateTime, ? super LocalDateTime, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function7<? super ULong, ? super ULong, ? super ULong, ? super ULong, ? super ULong, ? super LocalDateTime, ? super LocalDateTime, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
