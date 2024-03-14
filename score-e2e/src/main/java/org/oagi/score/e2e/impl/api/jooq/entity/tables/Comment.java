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
import org.jooq.Index;
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
import org.oagi.score.e2e.impl.api.jooq.entity.Indexes;
import org.oagi.score.e2e.impl.api.jooq.entity.Keys;
import org.oagi.score.e2e.impl.api.jooq.entity.Oagi;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AppUser.AppUserPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Comment.CommentPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.CommentRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Comment extends TableImpl<CommentRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.comment</code>
     */
    public static final Comment COMMENT = new Comment();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<CommentRecord> getRecordType() {
        return CommentRecord.class;
    }

    /**
     * The column <code>oagi.comment.comment_id</code>.
     */
    public final TableField<CommentRecord, ULong> COMMENT_ID = createField(DSL.name("comment_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "");

    /**
     * The column <code>oagi.comment.reference</code>.
     */
    public final TableField<CommentRecord, String> REFERENCE = createField(DSL.name("reference"), SQLDataType.VARCHAR(100).nullable(false).defaultValue(DSL.field(DSL.raw("''"), SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>oagi.comment.comment</code>.
     */
    public final TableField<CommentRecord, String> COMMENT_ = createField(DSL.name("comment"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "");

    /**
     * The column <code>oagi.comment.is_hidden</code>.
     */
    public final TableField<CommentRecord, Byte> IS_HIDDEN = createField(DSL.name("is_hidden"), SQLDataType.TINYINT.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.TINYINT)), this, "");

    /**
     * The column <code>oagi.comment.is_deleted</code>.
     */
    public final TableField<CommentRecord, Byte> IS_DELETED = createField(DSL.name("is_deleted"), SQLDataType.TINYINT.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.TINYINT)), this, "");

    /**
     * The column <code>oagi.comment.prev_comment_id</code>.
     */
    public final TableField<CommentRecord, ULong> PREV_COMMENT_ID = createField(DSL.name("prev_comment_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "");

    /**
     * The column <code>oagi.comment.created_by</code>.
     */
    public final TableField<CommentRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.comment.creation_timestamp</code>.
     */
    public final TableField<CommentRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "");

    /**
     * The column <code>oagi.comment.last_update_timestamp</code>.
     */
    public final TableField<CommentRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "");

    private Comment(Name alias, Table<CommentRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Comment(Name alias, Table<CommentRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.comment</code> table reference
     */
    public Comment(String alias) {
        this(DSL.name(alias), COMMENT);
    }

    /**
     * Create an aliased <code>oagi.comment</code> table reference
     */
    public Comment(Name alias) {
        this(alias, COMMENT);
    }

    /**
     * Create a <code>oagi.comment</code> table reference
     */
    public Comment() {
        this(DSL.name("comment"), null);
    }

    public <O extends Record> Comment(Table<O> path, ForeignKey<O, CommentRecord> childPath, InverseForeignKey<O, CommentRecord> parentPath) {
        super(path, childPath, parentPath, COMMENT);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class CommentPath extends Comment implements Path<CommentRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> CommentPath(Table<O> path, ForeignKey<O, CommentRecord> childPath, InverseForeignKey<O, CommentRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private CommentPath(Name alias, Table<CommentRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public CommentPath as(String alias) {
            return new CommentPath(DSL.name(alias), this);
        }

        @Override
        public CommentPath as(Name alias) {
            return new CommentPath(alias, this);
        }

        @Override
        public CommentPath as(Table<?> alias) {
            return new CommentPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.COMMENT_REFERENCE);
    }

    @Override
    public Identity<CommentRecord, ULong> getIdentity() {
        return (Identity<CommentRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<CommentRecord> getPrimaryKey() {
        return Keys.KEY_COMMENT_PRIMARY;
    }

    @Override
    public List<ForeignKey<CommentRecord, ?>> getReferences() {
        return Arrays.asList(Keys.COMMENT_PREV_COMMENT_ID_FK, Keys.COMMENT_CREATED_BY_FK);
    }

    private transient CommentPath _comment;

    /**
     * Get the implicit join path to the <code>oagi.comment</code> table.
     */
    public CommentPath comment() {
        if (_comment == null)
            _comment = new CommentPath(this, Keys.COMMENT_PREV_COMMENT_ID_FK, null);

        return _comment;
    }

    private transient AppUserPath _appUser;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table.
     */
    public AppUserPath appUser() {
        if (_appUser == null)
            _appUser = new AppUserPath(this, Keys.COMMENT_CREATED_BY_FK, null);

        return _appUser;
    }

    @Override
    public Comment as(String alias) {
        return new Comment(DSL.name(alias), this);
    }

    @Override
    public Comment as(Name alias) {
        return new Comment(alias, this);
    }

    @Override
    public Comment as(Table<?> alias) {
        return new Comment(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Comment rename(String name) {
        return new Comment(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Comment rename(Name name) {
        return new Comment(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Comment rename(Table<?> name) {
        return new Comment(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Comment where(Condition condition) {
        return new Comment(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Comment where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Comment where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Comment where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Comment where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Comment where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Comment where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Comment where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Comment whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Comment whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
