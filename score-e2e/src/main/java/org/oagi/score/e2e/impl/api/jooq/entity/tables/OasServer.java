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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.OasDoc.OasDocPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.OasServerVariable.OasServerVariablePath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.OasServerRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class OasServer extends TableImpl<OasServerRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.oas_server</code>
     */
    public static final OasServer OAS_SERVER = new OasServer();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OasServerRecord> getRecordType() {
        return OasServerRecord.class;
    }

    /**
     * The column <code>oagi.oas_server.oas_server_id</code>. The primary key of
     * the record.
     */
    public final TableField<OasServerRecord, ULong> OAS_SERVER_ID = createField(DSL.name("oas_server_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "The primary key of the record.");

    /**
     * The column <code>oagi.oas_server.guid</code>. The GUID of the record.
     */
    public final TableField<OasServerRecord, String> GUID = createField(DSL.name("guid"), SQLDataType.VARCHAR(41).nullable(false), this, "The GUID of the record.");

    /**
     * The column <code>oagi.oas_server.oas_doc_id</code>. A reference of the
     * doc record.
     */
    public final TableField<OasServerRecord, ULong> OAS_DOC_ID = createField(DSL.name("oas_doc_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A reference of the doc record.");

    /**
     * The column <code>oagi.oas_server.description</code>. An optional string
     * describing the host designated by the URL. CommonMark syntax MAY be used
     * for rich text representation.
     */
    public final TableField<OasServerRecord, String> DESCRIPTION = createField(DSL.name("description"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "An optional string describing the host designated by the URL. CommonMark syntax MAY be used for rich text representation.");

    /**
     * The column <code>oagi.oas_server.url</code>. REQUIRED. A URL to the
     * target host. This URL supports Server Variables and MAY be relative, to
     * indicate that the host location is relative to the location where the
     * OpenAPI document is being served. Variable substitutions will be made
     * when a variable is named in {brackets}.
     */
    public final TableField<OasServerRecord, String> URL = createField(DSL.name("url"), SQLDataType.VARCHAR(250).nullable(false), this, "REQUIRED. A URL to the target host. This URL supports Server Variables and MAY be relative, to indicate that the host location is relative to the location where the OpenAPI document is being served. Variable substitutions will be made when a variable is named in {brackets}.");

    /**
     * The column <code>oagi.oas_server.variables</code>. A map between a
     * variable name and its value. The value is used for substitution in the
     * server's URL template.
     */
    public final TableField<OasServerRecord, String> VARIABLES = createField(DSL.name("variables"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "A map between a variable name and its value. The value is used for substitution in the server's URL template.");

    /**
     * The column <code>oagi.oas_server.owner_user_id</code>. The user who owns
     * the record.
     */
    public final TableField<OasServerRecord, ULong> OWNER_USER_ID = createField(DSL.name("owner_user_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who owns the record.");

    /**
     * The column <code>oagi.oas_server.created_by</code>. The user who creates
     * the record.
     */
    public final TableField<OasServerRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who creates the record.");

    /**
     * The column <code>oagi.oas_server.last_updated_by</code>. The user who
     * last updates the record.
     */
    public final TableField<OasServerRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who last updates the record.");

    /**
     * The column <code>oagi.oas_server.creation_timestamp</code>. The timestamp
     * when the record is created.
     */
    public final TableField<OasServerRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record is created.");

    /**
     * The column <code>oagi.oas_server.last_update_timestamp</code>. The
     * timestamp when the record is last updated.
     */
    public final TableField<OasServerRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record is last updated.");

    private OasServer(Name alias, Table<OasServerRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private OasServer(Name alias, Table<OasServerRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.oas_server</code> table reference
     */
    public OasServer(String alias) {
        this(DSL.name(alias), OAS_SERVER);
    }

    /**
     * Create an aliased <code>oagi.oas_server</code> table reference
     */
    public OasServer(Name alias) {
        this(alias, OAS_SERVER);
    }

    /**
     * Create a <code>oagi.oas_server</code> table reference
     */
    public OasServer() {
        this(DSL.name("oas_server"), null);
    }

    public <O extends Record> OasServer(Table<O> path, ForeignKey<O, OasServerRecord> childPath, InverseForeignKey<O, OasServerRecord> parentPath) {
        super(path, childPath, parentPath, OAS_SERVER);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class OasServerPath extends OasServer implements Path<OasServerRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> OasServerPath(Table<O> path, ForeignKey<O, OasServerRecord> childPath, InverseForeignKey<O, OasServerRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private OasServerPath(Name alias, Table<OasServerRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public OasServerPath as(String alias) {
            return new OasServerPath(DSL.name(alias), this);
        }

        @Override
        public OasServerPath as(Name alias) {
            return new OasServerPath(alias, this);
        }

        @Override
        public OasServerPath as(Table<?> alias) {
            return new OasServerPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<OasServerRecord, ULong> getIdentity() {
        return (Identity<OasServerRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<OasServerRecord> getPrimaryKey() {
        return Keys.KEY_OAS_SERVER_PRIMARY;
    }

    @Override
    public List<ForeignKey<OasServerRecord, ?>> getReferences() {
        return Arrays.asList(Keys.OAS_SERVER_CREATED_BY_FK, Keys.OAS_SERVER_LAST_UPDATED_BY_FK, Keys.OAS_SERVER_OAS_DOC_ID_FK, Keys.OAS_SERVER_OWNER_USER_ID_FK);
    }

    private transient AppUserPath _oasServerCreatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>oas_server_created_by_fk</code> key.
     */
    public AppUserPath oasServerCreatedByFk() {
        if (_oasServerCreatedByFk == null)
            _oasServerCreatedByFk = new AppUserPath(this, Keys.OAS_SERVER_CREATED_BY_FK, null);

        return _oasServerCreatedByFk;
    }

    private transient AppUserPath _oasServerLastUpdatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>oas_server_last_updated_by_fk</code> key.
     */
    public AppUserPath oasServerLastUpdatedByFk() {
        if (_oasServerLastUpdatedByFk == null)
            _oasServerLastUpdatedByFk = new AppUserPath(this, Keys.OAS_SERVER_LAST_UPDATED_BY_FK, null);

        return _oasServerLastUpdatedByFk;
    }

    private transient OasDocPath _oasDoc;

    /**
     * Get the implicit join path to the <code>oagi.oas_doc</code> table.
     */
    public OasDocPath oasDoc() {
        if (_oasDoc == null)
            _oasDoc = new OasDocPath(this, Keys.OAS_SERVER_OAS_DOC_ID_FK, null);

        return _oasDoc;
    }

    private transient AppUserPath _oasServerOwnerUserIdFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>oas_server_owner_user_id_fk</code> key.
     */
    public AppUserPath oasServerOwnerUserIdFk() {
        if (_oasServerOwnerUserIdFk == null)
            _oasServerOwnerUserIdFk = new AppUserPath(this, Keys.OAS_SERVER_OWNER_USER_ID_FK, null);

        return _oasServerOwnerUserIdFk;
    }

    private transient OasServerVariablePath _oasServerVariable;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.oas_server_variable</code> table
     */
    public OasServerVariablePath oasServerVariable() {
        if (_oasServerVariable == null)
            _oasServerVariable = new OasServerVariablePath(this, null, Keys.OAS_SERVER_VARIABLE_OAS_SERVER_ID_FK.getInverseKey());

        return _oasServerVariable;
    }

    @Override
    public OasServer as(String alias) {
        return new OasServer(DSL.name(alias), this);
    }

    @Override
    public OasServer as(Name alias) {
        return new OasServer(alias, this);
    }

    @Override
    public OasServer as(Table<?> alias) {
        return new OasServer(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public OasServer rename(String name) {
        return new OasServer(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public OasServer rename(Name name) {
        return new OasServer(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public OasServer rename(Table<?> name) {
        return new OasServer(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasServer where(Condition condition) {
        return new OasServer(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasServer where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasServer where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasServer where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasServer where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasServer where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasServer where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasServer where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasServer whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasServer whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
