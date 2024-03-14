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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AgencyIdListValue.AgencyIdListValuePath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AppUser.AppUserPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.OasParameter.OasParameterPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.OasResponse.OasResponsePath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.OasResponseHeaders.OasResponseHeadersPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.OasHttpHeaderRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OasHttpHeader extends TableImpl<OasHttpHeaderRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.oas_http_header</code>
     */
    public static final OasHttpHeader OAS_HTTP_HEADER = new OasHttpHeader();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OasHttpHeaderRecord> getRecordType() {
        return OasHttpHeaderRecord.class;
    }

    /**
     * The column <code>oagi.oas_http_header.oas_http_header_id</code>. The
     * primary key of the record.
     */
    public final TableField<OasHttpHeaderRecord, ULong> OAS_HTTP_HEADER_ID = createField(DSL.name("oas_http_header_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "The primary key of the record.");

    /**
     * The column <code>oagi.oas_http_header.guid</code>. The GUID of the
     * record.
     */
    public final TableField<OasHttpHeaderRecord, String> GUID = createField(DSL.name("guid"), SQLDataType.VARCHAR(41).nullable(false), this, "The GUID of the record.");

    /**
     * The column <code>oagi.oas_http_header.header</code>. REQUIRED. The name
     * of the header. Header names are case sensitive. 
     */
    public final TableField<OasHttpHeaderRecord, String> HEADER = createField(DSL.name("header"), SQLDataType.VARCHAR(200).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "REQUIRED. The name of the header. Header names are case sensitive. ");

    /**
     * The column <code>oagi.oas_http_header.description</code>. A brief
     * description of the header. This could contain examples of use. CommonMark
     * syntax MAY be used for rich text representation.
     */
    public final TableField<OasHttpHeaderRecord, String> DESCRIPTION = createField(DSL.name("description"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "A brief description of the header. This could contain examples of use. CommonMark syntax MAY be used for rich text representation.");

    /**
     * The column <code>oagi.oas_http_header.agency_id_list_value_id</code>. A
     * reference of the agency id list value
     */
    public final TableField<OasHttpHeaderRecord, ULong> AGENCY_ID_LIST_VALUE_ID = createField(DSL.name("agency_id_list_value_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A reference of the agency id list value");

    /**
     * The column <code>oagi.oas_http_header.schema_type_reference</code>.
     * REQUIRED. The schema defining the type used for the header using the
     * reference string, $ref.
     */
    public final TableField<OasHttpHeaderRecord, String> SCHEMA_TYPE_REFERENCE = createField(DSL.name("schema_type_reference"), SQLDataType.CLOB.nullable(false), this, "REQUIRED. The schema defining the type used for the header using the reference string, $ref.");

    /**
     * The column <code>oagi.oas_http_header.owner_user_id</code>. The user who
     * owns the record.
     */
    public final TableField<OasHttpHeaderRecord, ULong> OWNER_USER_ID = createField(DSL.name("owner_user_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who owns the record.");

    /**
     * The column <code>oagi.oas_http_header.created_by</code>. The user who
     * creates the record.
     */
    public final TableField<OasHttpHeaderRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who creates the record.");

    /**
     * The column <code>oagi.oas_http_header.last_updated_by</code>. The user
     * who last updates the record.
     */
    public final TableField<OasHttpHeaderRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who last updates the record.");

    /**
     * The column <code>oagi.oas_http_header.creation_timestamp</code>. The
     * timestamp when the record is created.
     */
    public final TableField<OasHttpHeaderRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record is created.");

    /**
     * The column <code>oagi.oas_http_header.last_update_timestamp</code>. The
     * timestamp when the record is last updated.
     */
    public final TableField<OasHttpHeaderRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record is last updated.");

    private OasHttpHeader(Name alias, Table<OasHttpHeaderRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private OasHttpHeader(Name alias, Table<OasHttpHeaderRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.oas_http_header</code> table reference
     */
    public OasHttpHeader(String alias) {
        this(DSL.name(alias), OAS_HTTP_HEADER);
    }

    /**
     * Create an aliased <code>oagi.oas_http_header</code> table reference
     */
    public OasHttpHeader(Name alias) {
        this(alias, OAS_HTTP_HEADER);
    }

    /**
     * Create a <code>oagi.oas_http_header</code> table reference
     */
    public OasHttpHeader() {
        this(DSL.name("oas_http_header"), null);
    }

    public <O extends Record> OasHttpHeader(Table<O> path, ForeignKey<O, OasHttpHeaderRecord> childPath, InverseForeignKey<O, OasHttpHeaderRecord> parentPath) {
        super(path, childPath, parentPath, OAS_HTTP_HEADER);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class OasHttpHeaderPath extends OasHttpHeader implements Path<OasHttpHeaderRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> OasHttpHeaderPath(Table<O> path, ForeignKey<O, OasHttpHeaderRecord> childPath, InverseForeignKey<O, OasHttpHeaderRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private OasHttpHeaderPath(Name alias, Table<OasHttpHeaderRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public OasHttpHeaderPath as(String alias) {
            return new OasHttpHeaderPath(DSL.name(alias), this);
        }

        @Override
        public OasHttpHeaderPath as(Name alias) {
            return new OasHttpHeaderPath(alias, this);
        }

        @Override
        public OasHttpHeaderPath as(Table<?> alias) {
            return new OasHttpHeaderPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<OasHttpHeaderRecord, ULong> getIdentity() {
        return (Identity<OasHttpHeaderRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<OasHttpHeaderRecord> getPrimaryKey() {
        return Keys.KEY_OAS_HTTP_HEADER_PRIMARY;
    }

    @Override
    public List<ForeignKey<OasHttpHeaderRecord, ?>> getReferences() {
        return Arrays.asList(Keys.OAS_HTTP_HEADER_AGENCY_ID_LIST_VALUE_ID_FK, Keys.OAS_HTTP_HEADER_OWNER_USER_ID_FK, Keys.OAS_HTTP_HEADER_CREATED_BY_FK, Keys.OAS_HTTP_HEADER_LAST_UPDATED_BY_FK);
    }

    private transient AgencyIdListValuePath _agencyIdListValue;

    /**
     * Get the implicit join path to the <code>oagi.agency_id_list_value</code>
     * table.
     */
    public AgencyIdListValuePath agencyIdListValue() {
        if (_agencyIdListValue == null)
            _agencyIdListValue = new AgencyIdListValuePath(this, Keys.OAS_HTTP_HEADER_AGENCY_ID_LIST_VALUE_ID_FK, null);

        return _agencyIdListValue;
    }

    private transient AppUserPath _oasHttpHeaderOwnerUserIdFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>oas_http_header_owner_user_id_fk</code> key.
     */
    public AppUserPath oasHttpHeaderOwnerUserIdFk() {
        if (_oasHttpHeaderOwnerUserIdFk == null)
            _oasHttpHeaderOwnerUserIdFk = new AppUserPath(this, Keys.OAS_HTTP_HEADER_OWNER_USER_ID_FK, null);

        return _oasHttpHeaderOwnerUserIdFk;
    }

    private transient AppUserPath _oasHttpHeaderCreatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>oas_http_header_created_by_fk</code> key.
     */
    public AppUserPath oasHttpHeaderCreatedByFk() {
        if (_oasHttpHeaderCreatedByFk == null)
            _oasHttpHeaderCreatedByFk = new AppUserPath(this, Keys.OAS_HTTP_HEADER_CREATED_BY_FK, null);

        return _oasHttpHeaderCreatedByFk;
    }

    private transient AppUserPath _oasHttpHeaderLastUpdatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>oas_http_header_last_updated_by_fk</code> key.
     */
    public AppUserPath oasHttpHeaderLastUpdatedByFk() {
        if (_oasHttpHeaderLastUpdatedByFk == null)
            _oasHttpHeaderLastUpdatedByFk = new AppUserPath(this, Keys.OAS_HTTP_HEADER_LAST_UPDATED_BY_FK, null);

        return _oasHttpHeaderLastUpdatedByFk;
    }

    private transient OasParameterPath _oasParameter;

    /**
     * Get the implicit to-many join path to the <code>oagi.oas_parameter</code>
     * table
     */
    public OasParameterPath oasParameter() {
        if (_oasParameter == null)
            _oasParameter = new OasParameterPath(this, null, Keys.OAS_PARAMETER_OAS_HTTP_HEADER_ID_FK.getInverseKey());

        return _oasParameter;
    }

    private transient OasResponseHeadersPath _oasResponseHeaders;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.oas_response_headers</code> table
     */
    public OasResponseHeadersPath oasResponseHeaders() {
        if (_oasResponseHeaders == null)
            _oasResponseHeaders = new OasResponseHeadersPath(this, null, Keys.OAS_RESPONSE_HEADERS_OAS_HTTP_HEADER_ID_FK.getInverseKey());

        return _oasResponseHeaders;
    }

    /**
     * Get the implicit many-to-many join path to the
     * <code>oagi.oas_response</code> table
     */
    public OasResponsePath oasResponse() {
        return oasResponseHeaders().oasResponse();
    }

    @Override
    public OasHttpHeader as(String alias) {
        return new OasHttpHeader(DSL.name(alias), this);
    }

    @Override
    public OasHttpHeader as(Name alias) {
        return new OasHttpHeader(alias, this);
    }

    @Override
    public OasHttpHeader as(Table<?> alias) {
        return new OasHttpHeader(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public OasHttpHeader rename(String name) {
        return new OasHttpHeader(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public OasHttpHeader rename(Name name) {
        return new OasHttpHeader(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public OasHttpHeader rename(Table<?> name) {
        return new OasHttpHeader(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasHttpHeader where(Condition condition) {
        return new OasHttpHeader(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasHttpHeader where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasHttpHeader where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasHttpHeader where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasHttpHeader where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasHttpHeader where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasHttpHeader where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasHttpHeader where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasHttpHeader whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasHttpHeader whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
