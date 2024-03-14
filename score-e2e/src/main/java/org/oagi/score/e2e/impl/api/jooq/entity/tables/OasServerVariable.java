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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.OasServer.OasServerPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.OasServerVariableRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OasServerVariable extends TableImpl<OasServerVariableRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.oas_server_variable</code>
     */
    public static final OasServerVariable OAS_SERVER_VARIABLE = new OasServerVariable();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OasServerVariableRecord> getRecordType() {
        return OasServerVariableRecord.class;
    }

    /**
     * The column <code>oagi.oas_server_variable.oas_server_variable_id</code>.
     * The primary key of the record.
     */
    public final TableField<OasServerVariableRecord, ULong> OAS_SERVER_VARIABLE_ID = createField(DSL.name("oas_server_variable_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "The primary key of the record.");

    /**
     * The column <code>oagi.oas_server_variable.oas_server_id</code>. A
     * reference of the server record.
     */
    public final TableField<OasServerVariableRecord, ULong> OAS_SERVER_ID = createField(DSL.name("oas_server_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A reference of the server record.");

    /**
     * The column <code>oagi.oas_server_variable.name</code>. "port",
     * "username", "basePath" are the examples in the OpenAPI Specification.
     */
    public final TableField<OasServerVariableRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(100).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "\"port\", \"username\", \"basePath\" are the examples in the OpenAPI Specification.");

    /**
     * The column <code>oagi.oas_server_variable.description</code>. An optional
     * description for the server variable. CommonMark syntax MAY be used for
     * rich text representation.
     */
    public final TableField<OasServerVariableRecord, String> DESCRIPTION = createField(DSL.name("description"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "An optional description for the server variable. CommonMark syntax MAY be used for rich text representation.");

    /**
     * The column <code>oagi.oas_server_variable.default</code>. REQUIRED. The
     * default value to use for substitution, which SHALL be sent if an
     * alternate value is not supplied. Note this behavior is different than the
     * Schema Object's treatment of default values, because in those cases
     * parameter values are optional. If the enum is defined, the value SHOULD
     * exist in the enum's values.
     */
    public final TableField<OasServerVariableRecord, String> DEFAULT = createField(DSL.name("default"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "REQUIRED. The default value to use for substitution, which SHALL be sent if an alternate value is not supplied. Note this behavior is different than the Schema Object's treatment of default values, because in those cases parameter values are optional. If the enum is defined, the value SHOULD exist in the enum's values.");

    /**
     * The column <code>oagi.oas_server_variable.enum</code>. An enumeration of
     * string values to be used if the substitution options are from a limited
     * set. The array SHOULD NOT be empty.
     */
    public final TableField<OasServerVariableRecord, String> ENUM = createField(DSL.name("enum"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "An enumeration of string values to be used if the substitution options are from a limited set. The array SHOULD NOT be empty.");

    /**
     * The column <code>oagi.oas_server_variable.created_by</code>. The user who
     * creates the record.
     */
    public final TableField<OasServerVariableRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who creates the record.");

    /**
     * The column <code>oagi.oas_server_variable.last_updated_by</code>. The
     * user who last updates the record.
     */
    public final TableField<OasServerVariableRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who last updates the record.");

    /**
     * The column <code>oagi.oas_server_variable.creation_timestamp</code>. The
     * timestamp when the record is created.
     */
    public final TableField<OasServerVariableRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record is created.");

    /**
     * The column <code>oagi.oas_server_variable.last_update_timestamp</code>.
     * The timestamp when the record is last updated.
     */
    public final TableField<OasServerVariableRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record is last updated.");

    private OasServerVariable(Name alias, Table<OasServerVariableRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private OasServerVariable(Name alias, Table<OasServerVariableRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.oas_server_variable</code> table reference
     */
    public OasServerVariable(String alias) {
        this(DSL.name(alias), OAS_SERVER_VARIABLE);
    }

    /**
     * Create an aliased <code>oagi.oas_server_variable</code> table reference
     */
    public OasServerVariable(Name alias) {
        this(alias, OAS_SERVER_VARIABLE);
    }

    /**
     * Create a <code>oagi.oas_server_variable</code> table reference
     */
    public OasServerVariable() {
        this(DSL.name("oas_server_variable"), null);
    }

    public <O extends Record> OasServerVariable(Table<O> path, ForeignKey<O, OasServerVariableRecord> childPath, InverseForeignKey<O, OasServerVariableRecord> parentPath) {
        super(path, childPath, parentPath, OAS_SERVER_VARIABLE);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class OasServerVariablePath extends OasServerVariable implements Path<OasServerVariableRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> OasServerVariablePath(Table<O> path, ForeignKey<O, OasServerVariableRecord> childPath, InverseForeignKey<O, OasServerVariableRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private OasServerVariablePath(Name alias, Table<OasServerVariableRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public OasServerVariablePath as(String alias) {
            return new OasServerVariablePath(DSL.name(alias), this);
        }

        @Override
        public OasServerVariablePath as(Name alias) {
            return new OasServerVariablePath(alias, this);
        }

        @Override
        public OasServerVariablePath as(Table<?> alias) {
            return new OasServerVariablePath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<OasServerVariableRecord, ULong> getIdentity() {
        return (Identity<OasServerVariableRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<OasServerVariableRecord> getPrimaryKey() {
        return Keys.KEY_OAS_SERVER_VARIABLE_PRIMARY;
    }

    @Override
    public List<ForeignKey<OasServerVariableRecord, ?>> getReferences() {
        return Arrays.asList(Keys.OAS_SERVER_VARIABLE_OAS_SERVER_ID_FK, Keys.OAS_SERVER_VARIABLE_CREATED_BY_FK, Keys.OAS_SERVER_VARIABLE_LAST_UPDATED_BY_FK);
    }

    private transient OasServerPath _oasServer;

    /**
     * Get the implicit join path to the <code>oagi.oas_server</code> table.
     */
    public OasServerPath oasServer() {
        if (_oasServer == null)
            _oasServer = new OasServerPath(this, Keys.OAS_SERVER_VARIABLE_OAS_SERVER_ID_FK, null);

        return _oasServer;
    }

    private transient AppUserPath _oasServerVariableCreatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>oas_server_variable_created_by_fk</code> key.
     */
    public AppUserPath oasServerVariableCreatedByFk() {
        if (_oasServerVariableCreatedByFk == null)
            _oasServerVariableCreatedByFk = new AppUserPath(this, Keys.OAS_SERVER_VARIABLE_CREATED_BY_FK, null);

        return _oasServerVariableCreatedByFk;
    }

    private transient AppUserPath _oasServerVariableLastUpdatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>oas_server_variable_last_updated_by_fk</code> key.
     */
    public AppUserPath oasServerVariableLastUpdatedByFk() {
        if (_oasServerVariableLastUpdatedByFk == null)
            _oasServerVariableLastUpdatedByFk = new AppUserPath(this, Keys.OAS_SERVER_VARIABLE_LAST_UPDATED_BY_FK, null);

        return _oasServerVariableLastUpdatedByFk;
    }

    @Override
    public OasServerVariable as(String alias) {
        return new OasServerVariable(DSL.name(alias), this);
    }

    @Override
    public OasServerVariable as(Name alias) {
        return new OasServerVariable(alias, this);
    }

    @Override
    public OasServerVariable as(Table<?> alias) {
        return new OasServerVariable(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public OasServerVariable rename(String name) {
        return new OasServerVariable(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public OasServerVariable rename(Name name) {
        return new OasServerVariable(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public OasServerVariable rename(Table<?> name) {
        return new OasServerVariable(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasServerVariable where(Condition condition) {
        return new OasServerVariable(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasServerVariable where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasServerVariable where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasServerVariable where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasServerVariable where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasServerVariable where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasServerVariable where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasServerVariable where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasServerVariable whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasServerVariable whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
