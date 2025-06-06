/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.gateway.http.common.repository.jooq.entity.tables;


import java.time.LocalDateTime;
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
import org.oagi.score.gateway.http.common.repository.jooq.entity.Keys;
import org.oagi.score.gateway.http.common.repository.jooq.entity.Oagi;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AppUser.AppUserPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.OasOperation.OasOperationPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.OasParameter.OasParameterPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.OasResponse.OasResponsePath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasParameterLinkRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class OasParameterLink extends TableImpl<OasParameterLinkRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.oas_parameter_link</code>
     */
    public static final OasParameterLink OAS_PARAMETER_LINK = new OasParameterLink();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OasParameterLinkRecord> getRecordType() {
        return OasParameterLinkRecord.class;
    }

    /**
     * The column <code>oagi.oas_parameter_link.oas_parameter_link_id</code>.
     * The primary key of the record.
     */
    public final TableField<OasParameterLinkRecord, ULong> OAS_PARAMETER_LINK_ID = createField(DSL.name("oas_parameter_link_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "The primary key of the record.");

    /**
     * The column <code>oagi.oas_parameter_link.oas_response_id</code>.
     */
    public final TableField<OasParameterLinkRecord, ULong> OAS_RESPONSE_ID = createField(DSL.name("oas_response_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.oas_parameter_link.oas_parameter_id</code>.
     */
    public final TableField<OasParameterLinkRecord, ULong> OAS_PARAMETER_ID = createField(DSL.name("oas_parameter_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.oas_parameter_link.oas_operation_id</code>.
     */
    public final TableField<OasParameterLinkRecord, ULong> OAS_OPERATION_ID = createField(DSL.name("oas_operation_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "");

    /**
     * The column <code>oagi.oas_parameter_link.expression</code>. jsonPathSnip
     * for example '$response.body#/purchaseOrderHeader.identifier'
     */
    public final TableField<OasParameterLinkRecord, String> EXPRESSION = createField(DSL.name("expression"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "jsonPathSnip for example '$response.body#/purchaseOrderHeader.identifier'");

    /**
     * The column <code>oagi.oas_parameter_link.description</code>.
     */
    public final TableField<OasParameterLinkRecord, String> DESCRIPTION = createField(DSL.name("description"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "");

    /**
     * The column <code>oagi.oas_parameter_link.created_by</code>. The user who
     * creates the record.
     */
    public final TableField<OasParameterLinkRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who creates the record.");

    /**
     * The column <code>oagi.oas_parameter_link.last_updated_by</code>. The user
     * who last updates the record.
     */
    public final TableField<OasParameterLinkRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who last updates the record.");

    /**
     * The column <code>oagi.oas_parameter_link.creation_timestamp</code>. The
     * timestamp when the record is created.
     */
    public final TableField<OasParameterLinkRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record is created.");

    /**
     * The column <code>oagi.oas_parameter_link.last_update_timestamp</code>.
     * The timestamp when the record is last updated.
     */
    public final TableField<OasParameterLinkRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record is last updated.");

    private OasParameterLink(Name alias, Table<OasParameterLinkRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private OasParameterLink(Name alias, Table<OasParameterLinkRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.oas_parameter_link</code> table reference
     */
    public OasParameterLink(String alias) {
        this(DSL.name(alias), OAS_PARAMETER_LINK);
    }

    /**
     * Create an aliased <code>oagi.oas_parameter_link</code> table reference
     */
    public OasParameterLink(Name alias) {
        this(alias, OAS_PARAMETER_LINK);
    }

    /**
     * Create a <code>oagi.oas_parameter_link</code> table reference
     */
    public OasParameterLink() {
        this(DSL.name("oas_parameter_link"), null);
    }

    public <O extends Record> OasParameterLink(Table<O> path, ForeignKey<O, OasParameterLinkRecord> childPath, InverseForeignKey<O, OasParameterLinkRecord> parentPath) {
        super(path, childPath, parentPath, OAS_PARAMETER_LINK);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class OasParameterLinkPath extends OasParameterLink implements Path<OasParameterLinkRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> OasParameterLinkPath(Table<O> path, ForeignKey<O, OasParameterLinkRecord> childPath, InverseForeignKey<O, OasParameterLinkRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private OasParameterLinkPath(Name alias, Table<OasParameterLinkRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public OasParameterLinkPath as(String alias) {
            return new OasParameterLinkPath(DSL.name(alias), this);
        }

        @Override
        public OasParameterLinkPath as(Name alias) {
            return new OasParameterLinkPath(alias, this);
        }

        @Override
        public OasParameterLinkPath as(Table<?> alias) {
            return new OasParameterLinkPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<OasParameterLinkRecord, ULong> getIdentity() {
        return (Identity<OasParameterLinkRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<OasParameterLinkRecord> getPrimaryKey() {
        return Keys.KEY_OAS_PARAMETER_LINK_PRIMARY;
    }

    @Override
    public List<ForeignKey<OasParameterLinkRecord, ?>> getReferences() {
        return Arrays.asList(Keys.OAS_PARAMETER_LINK_CREATED_BY_FK, Keys.OAS_PARAMETER_LINK_LAST_UPDATED_BY_FK, Keys.OAS_PARAMETER_LINK_OAS_OPERATION_ID_FK, Keys.OAS_PARAMETER_LINK_OAS_PARAMETER_ID_FK, Keys.OAS_PARAMETER_LINK_OAS_RESPONSE_ID_FK);
    }

    private transient AppUserPath _oasParameterLinkCreatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>oas_parameter_link_created_by_fk</code> key.
     */
    public AppUserPath oasParameterLinkCreatedByFk() {
        if (_oasParameterLinkCreatedByFk == null)
            _oasParameterLinkCreatedByFk = new AppUserPath(this, Keys.OAS_PARAMETER_LINK_CREATED_BY_FK, null);

        return _oasParameterLinkCreatedByFk;
    }

    private transient AppUserPath _oasParameterLinkLastUpdatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>oas_parameter_link_last_updated_by_fk</code> key.
     */
    public AppUserPath oasParameterLinkLastUpdatedByFk() {
        if (_oasParameterLinkLastUpdatedByFk == null)
            _oasParameterLinkLastUpdatedByFk = new AppUserPath(this, Keys.OAS_PARAMETER_LINK_LAST_UPDATED_BY_FK, null);

        return _oasParameterLinkLastUpdatedByFk;
    }

    private transient OasOperationPath _oasOperation;

    /**
     * Get the implicit join path to the <code>oagi.oas_operation</code> table.
     */
    public OasOperationPath oasOperation() {
        if (_oasOperation == null)
            _oasOperation = new OasOperationPath(this, Keys.OAS_PARAMETER_LINK_OAS_OPERATION_ID_FK, null);

        return _oasOperation;
    }

    private transient OasParameterPath _oasParameter;

    /**
     * Get the implicit join path to the <code>oagi.oas_parameter</code> table.
     */
    public OasParameterPath oasParameter() {
        if (_oasParameter == null)
            _oasParameter = new OasParameterPath(this, Keys.OAS_PARAMETER_LINK_OAS_PARAMETER_ID_FK, null);

        return _oasParameter;
    }

    private transient OasResponsePath _oasResponse;

    /**
     * Get the implicit join path to the <code>oagi.oas_response</code> table.
     */
    public OasResponsePath oasResponse() {
        if (_oasResponse == null)
            _oasResponse = new OasResponsePath(this, Keys.OAS_PARAMETER_LINK_OAS_RESPONSE_ID_FK, null);

        return _oasResponse;
    }

    @Override
    public OasParameterLink as(String alias) {
        return new OasParameterLink(DSL.name(alias), this);
    }

    @Override
    public OasParameterLink as(Name alias) {
        return new OasParameterLink(alias, this);
    }

    @Override
    public OasParameterLink as(Table<?> alias) {
        return new OasParameterLink(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public OasParameterLink rename(String name) {
        return new OasParameterLink(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public OasParameterLink rename(Name name) {
        return new OasParameterLink(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public OasParameterLink rename(Table<?> name) {
        return new OasParameterLink(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasParameterLink where(Condition condition) {
        return new OasParameterLink(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasParameterLink where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasParameterLink where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasParameterLink where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasParameterLink where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasParameterLink where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasParameterLink where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasParameterLink where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasParameterLink whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasParameterLink whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
