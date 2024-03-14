/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables;


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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Tenant.TenantPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.UserTenantRecord;


/**
 * This table captures the tenant roles of the user
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserTenant extends TableImpl<UserTenantRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.user_tenant</code>
     */
    public static final UserTenant USER_TENANT = new UserTenant();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<UserTenantRecord> getRecordType() {
        return UserTenantRecord.class;
    }

    /**
     * The column <code>oagi.user_tenant.user_tenant_id</code>. Primary key
     * column.
     */
    public final TableField<UserTenantRecord, ULong> USER_TENANT_ID = createField(DSL.name("user_tenant_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "Primary key column.");

    /**
     * The column <code>oagi.user_tenant.tenant_id</code>. Assigned tenant to
     * the user.
     */
    public final TableField<UserTenantRecord, ULong> TENANT_ID = createField(DSL.name("tenant_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "Assigned tenant to the user.");

    /**
     * The column <code>oagi.user_tenant.app_user_id</code>. Application user.
     */
    public final TableField<UserTenantRecord, ULong> APP_USER_ID = createField(DSL.name("app_user_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "Application user.");

    private UserTenant(Name alias, Table<UserTenantRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private UserTenant(Name alias, Table<UserTenantRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment("This table captures the tenant roles of the user"), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.user_tenant</code> table reference
     */
    public UserTenant(String alias) {
        this(DSL.name(alias), USER_TENANT);
    }

    /**
     * Create an aliased <code>oagi.user_tenant</code> table reference
     */
    public UserTenant(Name alias) {
        this(alias, USER_TENANT);
    }

    /**
     * Create a <code>oagi.user_tenant</code> table reference
     */
    public UserTenant() {
        this(DSL.name("user_tenant"), null);
    }

    public <O extends Record> UserTenant(Table<O> path, ForeignKey<O, UserTenantRecord> childPath, InverseForeignKey<O, UserTenantRecord> parentPath) {
        super(path, childPath, parentPath, USER_TENANT);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class UserTenantPath extends UserTenant implements Path<UserTenantRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> UserTenantPath(Table<O> path, ForeignKey<O, UserTenantRecord> childPath, InverseForeignKey<O, UserTenantRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private UserTenantPath(Name alias, Table<UserTenantRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public UserTenantPath as(String alias) {
            return new UserTenantPath(DSL.name(alias), this);
        }

        @Override
        public UserTenantPath as(Name alias) {
            return new UserTenantPath(alias, this);
        }

        @Override
        public UserTenantPath as(Table<?> alias) {
            return new UserTenantPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<UserTenantRecord, ULong> getIdentity() {
        return (Identity<UserTenantRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<UserTenantRecord> getPrimaryKey() {
        return Keys.KEY_USER_TENANT_PRIMARY;
    }

    @Override
    public List<UniqueKey<UserTenantRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.KEY_USER_TENANT_USER_TENANT_PAIR);
    }

    @Override
    public List<ForeignKey<UserTenantRecord, ?>> getReferences() {
        return Arrays.asList(Keys.USER_TENANT_TENANT_ID_FK, Keys.USER_TENANT_TENANT_ID_APP_USER_ID_FK);
    }

    private transient TenantPath _tenant;

    /**
     * Get the implicit join path to the <code>oagi.tenant</code> table.
     */
    public TenantPath tenant() {
        if (_tenant == null)
            _tenant = new TenantPath(this, Keys.USER_TENANT_TENANT_ID_FK, null);

        return _tenant;
    }

    private transient AppUserPath _appUser;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table.
     */
    public AppUserPath appUser() {
        if (_appUser == null)
            _appUser = new AppUserPath(this, Keys.USER_TENANT_TENANT_ID_APP_USER_ID_FK, null);

        return _appUser;
    }

    @Override
    public UserTenant as(String alias) {
        return new UserTenant(DSL.name(alias), this);
    }

    @Override
    public UserTenant as(Name alias) {
        return new UserTenant(alias, this);
    }

    @Override
    public UserTenant as(Table<?> alias) {
        return new UserTenant(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public UserTenant rename(String name) {
        return new UserTenant(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public UserTenant rename(Name name) {
        return new UserTenant(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public UserTenant rename(Table<?> name) {
        return new UserTenant(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserTenant where(Condition condition) {
        return new UserTenant(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserTenant where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserTenant where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserTenant where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserTenant where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserTenant where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserTenant where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserTenant where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserTenant whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserTenant whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
