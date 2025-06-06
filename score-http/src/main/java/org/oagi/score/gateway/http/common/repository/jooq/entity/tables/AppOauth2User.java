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
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Oauth2App.Oauth2AppPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AppOauth2UserRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class AppOauth2User extends TableImpl<AppOauth2UserRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.app_oauth2_user</code>
     */
    public static final AppOauth2User APP_OAUTH2_USER = new AppOauth2User();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AppOauth2UserRecord> getRecordType() {
        return AppOauth2UserRecord.class;
    }

    /**
     * The column <code>oagi.app_oauth2_user.app_oauth2_user_id</code>. Primary
     * key.
     */
    public final TableField<AppOauth2UserRecord, ULong> APP_OAUTH2_USER_ID = createField(DSL.name("app_oauth2_user_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "Primary key.");

    /**
     * The column <code>oagi.app_oauth2_user.app_user_id</code>. A reference to
     * the record in `app_user`. If it is not set, this is treated as a pending
     * record.
     */
    public final TableField<AppOauth2UserRecord, ULong> APP_USER_ID = createField(DSL.name("app_user_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "A reference to the record in `app_user`. If it is not set, this is treated as a pending record.");

    /**
     * The column <code>oagi.app_oauth2_user.oauth2_app_id</code>. A reference
     * to the record in `oauth2_app`.
     */
    public final TableField<AppOauth2UserRecord, ULong> OAUTH2_APP_ID = createField(DSL.name("oauth2_app_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A reference to the record in `oauth2_app`.");

    /**
     * The column <code>oagi.app_oauth2_user.sub</code>. `sub` claim defined in
     * OIDC spec. This is a unique identifier of the subject in the provider.
     */
    public final TableField<AppOauth2UserRecord, String> SUB = createField(DSL.name("sub"), SQLDataType.VARCHAR(100).nullable(false), this, "`sub` claim defined in OIDC spec. This is a unique identifier of the subject in the provider.");

    /**
     * The column <code>oagi.app_oauth2_user.name</code>. `name` claim defined
     * in OIDC spec.
     */
    public final TableField<AppOauth2UserRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(200).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "`name` claim defined in OIDC spec.");

    /**
     * The column <code>oagi.app_oauth2_user.email</code>. `email` claim defined
     * in OIDC spec.
     */
    public final TableField<AppOauth2UserRecord, String> EMAIL = createField(DSL.name("email"), SQLDataType.VARCHAR(200).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "`email` claim defined in OIDC spec.");

    /**
     * The column <code>oagi.app_oauth2_user.nickname</code>. `nickname` claim
     * defined in OIDC spec.
     */
    public final TableField<AppOauth2UserRecord, String> NICKNAME = createField(DSL.name("nickname"), SQLDataType.VARCHAR(200).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "`nickname` claim defined in OIDC spec.");

    /**
     * The column <code>oagi.app_oauth2_user.preferred_username</code>.
     * `preferred_username` claim defined in OIDC spec.
     */
    public final TableField<AppOauth2UserRecord, String> PREFERRED_USERNAME = createField(DSL.name("preferred_username"), SQLDataType.VARCHAR(200).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "`preferred_username` claim defined in OIDC spec.");

    /**
     * The column <code>oagi.app_oauth2_user.phone_number</code>. `phone_number`
     * claim defined in OIDC spec.
     */
    public final TableField<AppOauth2UserRecord, String> PHONE_NUMBER = createField(DSL.name("phone_number"), SQLDataType.VARCHAR(200).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "`phone_number` claim defined in OIDC spec.");

    /**
     * The column <code>oagi.app_oauth2_user.creation_timestamp</code>.
     * Timestamp when this record is created.
     */
    public final TableField<AppOauth2UserRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "Timestamp when this record is created.");

    private AppOauth2User(Name alias, Table<AppOauth2UserRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private AppOauth2User(Name alias, Table<AppOauth2UserRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.app_oauth2_user</code> table reference
     */
    public AppOauth2User(String alias) {
        this(DSL.name(alias), APP_OAUTH2_USER);
    }

    /**
     * Create an aliased <code>oagi.app_oauth2_user</code> table reference
     */
    public AppOauth2User(Name alias) {
        this(alias, APP_OAUTH2_USER);
    }

    /**
     * Create a <code>oagi.app_oauth2_user</code> table reference
     */
    public AppOauth2User() {
        this(DSL.name("app_oauth2_user"), null);
    }

    public <O extends Record> AppOauth2User(Table<O> path, ForeignKey<O, AppOauth2UserRecord> childPath, InverseForeignKey<O, AppOauth2UserRecord> parentPath) {
        super(path, childPath, parentPath, APP_OAUTH2_USER);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class AppOauth2UserPath extends AppOauth2User implements Path<AppOauth2UserRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> AppOauth2UserPath(Table<O> path, ForeignKey<O, AppOauth2UserRecord> childPath, InverseForeignKey<O, AppOauth2UserRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private AppOauth2UserPath(Name alias, Table<AppOauth2UserRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public AppOauth2UserPath as(String alias) {
            return new AppOauth2UserPath(DSL.name(alias), this);
        }

        @Override
        public AppOauth2UserPath as(Name alias) {
            return new AppOauth2UserPath(alias, this);
        }

        @Override
        public AppOauth2UserPath as(Table<?> alias) {
            return new AppOauth2UserPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<AppOauth2UserRecord, ULong> getIdentity() {
        return (Identity<AppOauth2UserRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<AppOauth2UserRecord> getPrimaryKey() {
        return Keys.KEY_APP_OAUTH2_USER_PRIMARY;
    }

    @Override
    public List<UniqueKey<AppOauth2UserRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.KEY_APP_OAUTH2_USER_APP_OAUTH2_USER_UK1);
    }

    @Override
    public List<ForeignKey<AppOauth2UserRecord, ?>> getReferences() {
        return Arrays.asList(Keys.APP_OAUTH2_USER_APP_USER_ID_FK, Keys.APP_OAUTH2_USER_OAUTH2_APP_ID_FK);
    }

    private transient AppUserPath _appUser;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table.
     */
    public AppUserPath appUser() {
        if (_appUser == null)
            _appUser = new AppUserPath(this, Keys.APP_OAUTH2_USER_APP_USER_ID_FK, null);

        return _appUser;
    }

    private transient Oauth2AppPath _oauth2App;

    /**
     * Get the implicit join path to the <code>oagi.oauth2_app</code> table.
     */
    public Oauth2AppPath oauth2App() {
        if (_oauth2App == null)
            _oauth2App = new Oauth2AppPath(this, Keys.APP_OAUTH2_USER_OAUTH2_APP_ID_FK, null);

        return _oauth2App;
    }

    @Override
    public AppOauth2User as(String alias) {
        return new AppOauth2User(DSL.name(alias), this);
    }

    @Override
    public AppOauth2User as(Name alias) {
        return new AppOauth2User(alias, this);
    }

    @Override
    public AppOauth2User as(Table<?> alias) {
        return new AppOauth2User(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public AppOauth2User rename(String name) {
        return new AppOauth2User(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public AppOauth2User rename(Name name) {
        return new AppOauth2User(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public AppOauth2User rename(Table<?> name) {
        return new AppOauth2User(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AppOauth2User where(Condition condition) {
        return new AppOauth2User(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AppOauth2User where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AppOauth2User where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AppOauth2User where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public AppOauth2User where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public AppOauth2User where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public AppOauth2User where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public AppOauth2User where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AppOauth2User whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AppOauth2User whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
