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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.ReleaseRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


/**
 * The is table store the release information.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Release extends TableImpl<ReleaseRecord> {

    /**
     * The reference instance of <code>oagi.release</code>
     */
    public static final Release RELEASE = new Release();
    private static final long serialVersionUID = 1L;
    /**
     * The column <code>oagi.release.release_id</code>. RELEASE_ID must be an
     * incremental integer. RELEASE_ID that is more than another RELEASE_ID is
     * interpreted to be released later than the other.
     */
    public final TableField<ReleaseRecord, ULong> RELEASE_ID = createField(DSL.name("release_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "RELEASE_ID must be an incremental integer. RELEASE_ID that is more than another RELEASE_ID is interpreted to be released later than the other.");
    /**
     * The column <code>oagi.release.guid</code>. A globally unique identifier
     * (GUID).
     */
    public final TableField<ReleaseRecord, String> GUID = createField(DSL.name("guid"), SQLDataType.CHAR(32).nullable(false), this, "A globally unique identifier (GUID).");
    /**
     * The column <code>oagi.release.release_num</code>. Release number such has
     * 10.0, 10.1, etc.
     */
    public final TableField<ReleaseRecord, String> RELEASE_NUM = createField(DSL.name("release_num"), SQLDataType.VARCHAR(45), this, "Release number such has 10.0, 10.1, etc. ");
    /**
     * The column <code>oagi.release.release_note</code>. Description or note
     * associated with the release.
     */
    public final TableField<ReleaseRecord, String> RELEASE_NOTE = createField(DSL.name("release_note"), SQLDataType.CLOB, this, "Description or note associated with the release.");
    /**
     * The column <code>oagi.release.release_license</code>. License associated
     * with the release.
     */
    public final TableField<ReleaseRecord, String> RELEASE_LICENSE = createField(DSL.name("release_license"), SQLDataType.CLOB, this, "License associated with the release.");
    /**
     * The column <code>oagi.release.namespace_id</code>. Foreign key to the
     * NAMESPACE table. It identifies the namespace used with the release. It is
     * particularly useful for a library that uses a single namespace such like
     * the OAGIS 10.x. A library that uses multiple namespace but has a main
     * namespace may also use this column as a specific namespace can be
     * override at the module level.
     */
    public final TableField<ReleaseRecord, ULong> NAMESPACE_ID = createField(DSL.name("namespace_id"), SQLDataType.BIGINTUNSIGNED, this, "Foreign key to the NAMESPACE table. It identifies the namespace used with the release. It is particularly useful for a library that uses a single namespace such like the OAGIS 10.x. A library that uses multiple namespace but has a main namespace may also use this column as a specific namespace can be override at the module level.");
    /**
     * The column <code>oagi.release.created_by</code>. Foreign key to the
     * APP_USER table identifying user who created the namespace.
     */
    public final TableField<ReleaseRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "Foreign key to the APP_USER table identifying user who created the namespace.");
    /**
     * The column <code>oagi.release.last_updated_by</code>. Foreign key to the
     * APP_USER table identifying the user who last updated the record.
     */
    public final TableField<ReleaseRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "Foreign key to the APP_USER table identifying the user who last updated the record.");
    /**
     * The column <code>oagi.release.creation_timestamp</code>. The timestamp
     * when the record was first created.
     */
    public final TableField<ReleaseRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record was first created.");
    /**
     * The column <code>oagi.release.last_update_timestamp</code>. The timestamp
     * when the record was last updated.
     */
    public final TableField<ReleaseRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the record was last updated.");
    /**
     * The column <code>oagi.release.state</code>. This indicates the revision
     * life cycle state of the Release.
     */
    public final TableField<ReleaseRecord, String> STATE = createField(DSL.name("state"), SQLDataType.VARCHAR(20).defaultValue(DSL.inline("Initialized", SQLDataType.VARCHAR)), this, "This indicates the revision life cycle state of the Release.");
    private transient Namespace _namespace;
    private transient AppUser _releaseCreatedByFk;
    private transient AppUser _releaseLastUpdatedByFk;

    private Release(Name alias, Table<ReleaseRecord> aliased) {
        this(alias, aliased, null);
    }

    private Release(Name alias, Table<ReleaseRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("The is table store the release information."), TableOptions.table());
    }

    /**
     * Create an aliased <code>oagi.release</code> table reference
     */
    public Release(String alias) {
        this(DSL.name(alias), RELEASE);
    }

    /**
     * Create an aliased <code>oagi.release</code> table reference
     */
    public Release(Name alias) {
        this(alias, RELEASE);
    }

    /**
     * Create a <code>oagi.release</code> table reference
     */
    public Release() {
        this(DSL.name("release"), null);
    }

    public <O extends Record> Release(Table<O> child, ForeignKey<O, ReleaseRecord> key) {
        super(child, key, RELEASE);
    }

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ReleaseRecord> getRecordType() {
        return ReleaseRecord.class;
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<ReleaseRecord, ULong> getIdentity() {
        return (Identity<ReleaseRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<ReleaseRecord> getPrimaryKey() {
        return Keys.KEY_RELEASE_PRIMARY;
    }

    @Override
    public List<ForeignKey<ReleaseRecord, ?>> getReferences() {
        return Arrays.asList(Keys.RELEASE_NAMESPACE_ID_FK, Keys.RELEASE_CREATED_BY_FK, Keys.RELEASE_LAST_UPDATED_BY_FK);
    }

    /**
     * Get the implicit join path to the <code>oagi.namespace</code> table.
     */
    public Namespace namespace() {
        if (_namespace == null)
            _namespace = new Namespace(this, Keys.RELEASE_NAMESPACE_ID_FK);

        return _namespace;
    }

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>release_created_by_fk</code> key.
     */
    public AppUser releaseCreatedByFk() {
        if (_releaseCreatedByFk == null)
            _releaseCreatedByFk = new AppUser(this, Keys.RELEASE_CREATED_BY_FK);

        return _releaseCreatedByFk;
    }

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>release_last_updated_by_fk</code> key.
     */
    public AppUser releaseLastUpdatedByFk() {
        if (_releaseLastUpdatedByFk == null)
            _releaseLastUpdatedByFk = new AppUser(this, Keys.RELEASE_LAST_UPDATED_BY_FK);

        return _releaseLastUpdatedByFk;
    }

    @Override
    public Release as(String alias) {
        return new Release(DSL.name(alias), this);
    }

    @Override
    public Release as(Name alias) {
        return new Release(alias, this);
    }

    @Override
    public Release as(Table<?> alias) {
        return new Release(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Release rename(String name) {
        return new Release(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Release rename(Name name) {
        return new Release(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Release rename(Table<?> name) {
        return new Release(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row11 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row11<ULong, String, String, String, String, ULong, ULong, ULong, LocalDateTime, LocalDateTime, String> fieldsRow() {
        return (Row11) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function11<? super ULong, ? super String, ? super String, ? super String, ? super String, ? super ULong, ? super ULong, ? super ULong, ? super LocalDateTime, ? super LocalDateTime, ? super String, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function11<? super ULong, ? super String, ? super String, ? super String, ? super String, ? super ULong, ? super ULong, ? super ULong, ? super LocalDateTime, ? super LocalDateTime, ? super String, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
