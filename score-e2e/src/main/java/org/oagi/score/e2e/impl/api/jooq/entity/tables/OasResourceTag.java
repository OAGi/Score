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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.OasOperation.OasOperationPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.OasTag.OasTagPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.OasResourceTagRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OasResourceTag extends TableImpl<OasResourceTagRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.oas_resource_tag</code>
     */
    public static final OasResourceTag OAS_RESOURCE_TAG = new OasResourceTag();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OasResourceTagRecord> getRecordType() {
        return OasResourceTagRecord.class;
    }

    /**
     * The column <code>oagi.oas_resource_tag.oas_operation_id</code>. The
     * primary key of the record.
     */
    public final TableField<OasResourceTagRecord, ULong> OAS_OPERATION_ID = createField(DSL.name("oas_operation_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The primary key of the record.");

    /**
     * The column <code>oagi.oas_resource_tag.oas_tag_id</code>. The primary key
     * of the record.
     */
    public final TableField<OasResourceTagRecord, ULong> OAS_TAG_ID = createField(DSL.name("oas_tag_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The primary key of the record.");

    /**
     * The column <code>oagi.oas_resource_tag.created_by</code>. The user who
     * creates the record.
     */
    public final TableField<OasResourceTagRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who creates the record.");

    /**
     * The column <code>oagi.oas_resource_tag.last_updated_by</code>. The user
     * who last updates the record.
     */
    public final TableField<OasResourceTagRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The user who last updates the record.");

    /**
     * The column <code>oagi.oas_resource_tag.creation_timestamp</code>. The
     * timestamp when the record is created.
     */
    public final TableField<OasResourceTagRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record is created.");

    /**
     * The column <code>oagi.oas_resource_tag.last_update_timestamp</code>. The
     * timestamp when the record is last updated.
     */
    public final TableField<OasResourceTagRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record is last updated.");

    private OasResourceTag(Name alias, Table<OasResourceTagRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private OasResourceTag(Name alias, Table<OasResourceTagRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.oas_resource_tag</code> table reference
     */
    public OasResourceTag(String alias) {
        this(DSL.name(alias), OAS_RESOURCE_TAG);
    }

    /**
     * Create an aliased <code>oagi.oas_resource_tag</code> table reference
     */
    public OasResourceTag(Name alias) {
        this(alias, OAS_RESOURCE_TAG);
    }

    /**
     * Create a <code>oagi.oas_resource_tag</code> table reference
     */
    public OasResourceTag() {
        this(DSL.name("oas_resource_tag"), null);
    }

    public <O extends Record> OasResourceTag(Table<O> path, ForeignKey<O, OasResourceTagRecord> childPath, InverseForeignKey<O, OasResourceTagRecord> parentPath) {
        super(path, childPath, parentPath, OAS_RESOURCE_TAG);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class OasResourceTagPath extends OasResourceTag implements Path<OasResourceTagRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> OasResourceTagPath(Table<O> path, ForeignKey<O, OasResourceTagRecord> childPath, InverseForeignKey<O, OasResourceTagRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private OasResourceTagPath(Name alias, Table<OasResourceTagRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public OasResourceTagPath as(String alias) {
            return new OasResourceTagPath(DSL.name(alias), this);
        }

        @Override
        public OasResourceTagPath as(Name alias) {
            return new OasResourceTagPath(alias, this);
        }

        @Override
        public OasResourceTagPath as(Table<?> alias) {
            return new OasResourceTagPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public UniqueKey<OasResourceTagRecord> getPrimaryKey() {
        return Keys.KEY_OAS_RESOURCE_TAG_PRIMARY;
    }

    @Override
    public List<ForeignKey<OasResourceTagRecord, ?>> getReferences() {
        return Arrays.asList(Keys.OAS_RESOURCE_TAG_OAS_OPERATION_ID_FK, Keys.OAS_RESOURCE_TAG_OAS_TAG_ID_FK);
    }

    private transient OasOperationPath _oasOperation;

    /**
     * Get the implicit join path to the <code>oagi.oas_operation</code> table.
     */
    public OasOperationPath oasOperation() {
        if (_oasOperation == null)
            _oasOperation = new OasOperationPath(this, Keys.OAS_RESOURCE_TAG_OAS_OPERATION_ID_FK, null);

        return _oasOperation;
    }

    private transient OasTagPath _oasTag;

    /**
     * Get the implicit join path to the <code>oagi.oas_tag</code> table.
     */
    public OasTagPath oasTag() {
        if (_oasTag == null)
            _oasTag = new OasTagPath(this, Keys.OAS_RESOURCE_TAG_OAS_TAG_ID_FK, null);

        return _oasTag;
    }

    @Override
    public OasResourceTag as(String alias) {
        return new OasResourceTag(DSL.name(alias), this);
    }

    @Override
    public OasResourceTag as(Name alias) {
        return new OasResourceTag(alias, this);
    }

    @Override
    public OasResourceTag as(Table<?> alias) {
        return new OasResourceTag(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public OasResourceTag rename(String name) {
        return new OasResourceTag(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public OasResourceTag rename(Name name) {
        return new OasResourceTag(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public OasResourceTag rename(Table<?> name) {
        return new OasResourceTag(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasResourceTag where(Condition condition) {
        return new OasResourceTag(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasResourceTag where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasResourceTag where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasResourceTag where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasResourceTag where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasResourceTag where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasResourceTag where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public OasResourceTag where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasResourceTag whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public OasResourceTag whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
