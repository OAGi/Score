/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.gateway.http.common.repository.jooq.entity.tables;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Check;
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
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.common.repository.jooq.entity.Indexes;
import org.oagi.score.gateway.http.common.repository.jooq.entity.Keys;
import org.oagi.score.gateway.http.common.repository.jooq.entity.Oagi;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AccManifest.AccManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AgencyIdListManifest.AgencyIdListManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AppUser.AppUserPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AsccpManifest.AsccpManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BccpManifest.BccpManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.CodeListManifest.CodeListManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.DtManifest.DtManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Log.LogPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.XbtManifest.XbtManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.LogRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Log extends TableImpl<LogRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.log</code>
     */
    public static final Log LOG = new Log();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<LogRecord> getRecordType() {
        return LogRecord.class;
    }

    /**
     * The column <code>oagi.log.log_id</code>.
     */
    public final TableField<LogRecord, ULong> LOG_ID = createField(DSL.name("log_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "");

    /**
     * The column <code>oagi.log.hash</code>. The unique hash to identify the
     * log.
     */
    public final TableField<LogRecord, String> HASH = createField(DSL.name("hash"), SQLDataType.CHAR(40).nullable(false), this, "The unique hash to identify the log.");

    /**
     * The column <code>oagi.log.revision_num</code>. This is an incremental
     * integer. It tracks changes in each component. If a change is made to a
     * component after it has been published, the component receives a new
     * revision number. Revision number can be 1, 2, and so on.
     */
    public final TableField<LogRecord, UInteger> REVISION_NUM = createField(DSL.name("revision_num"), SQLDataType.INTEGERUNSIGNED.nullable(false).defaultValue(DSL.field(DSL.raw("1"), SQLDataType.INTEGERUNSIGNED)), this, "This is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 1, 2, and so on.");

    /**
     * The column <code>oagi.log.revision_tracking_num</code>. This supports the
     * ability to undo changes during a revision (life cycle of a revision is
     * from the component's WIP state to PUBLISHED state). REVISION_TRACKING_NUM
     * can be 1, 2, and so on.
     */
    public final TableField<LogRecord, UInteger> REVISION_TRACKING_NUM = createField(DSL.name("revision_tracking_num"), SQLDataType.INTEGERUNSIGNED.nullable(false).defaultValue(DSL.field(DSL.raw("1"), SQLDataType.INTEGERUNSIGNED)), this, "This supports the ability to undo changes during a revision (life cycle of a revision is from the component's WIP state to PUBLISHED state). REVISION_TRACKING_NUM can be 1, 2, and so on.");

    /**
     * The column <code>oagi.log.log_action</code>. This indicates the action
     * associated with the record.
     */
    public final TableField<LogRecord, String> LOG_ACTION = createField(DSL.name("log_action"), SQLDataType.VARCHAR(20).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "This indicates the action associated with the record.");

    /**
     * The column <code>oagi.log.reference</code>.
     */
    public final TableField<LogRecord, String> REFERENCE = createField(DSL.name("reference"), SQLDataType.VARCHAR(100).nullable(false).defaultValue(DSL.field(DSL.raw("''"), SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>oagi.log.snapshot</code>.
     */
    public final TableField<LogRecord, String> SNAPSHOT = createField(DSL.name("snapshot"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "");

    /**
     * The column <code>oagi.log.prev_log_id</code>.
     */
    public final TableField<LogRecord, ULong> PREV_LOG_ID = createField(DSL.name("prev_log_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "");

    /**
     * The column <code>oagi.log.next_log_id</code>.
     */
    public final TableField<LogRecord, ULong> NEXT_LOG_ID = createField(DSL.name("next_log_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "");

    /**
     * The column <code>oagi.log.created_by</code>.
     */
    public final TableField<LogRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.log.creation_timestamp</code>.
     */
    public final TableField<LogRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "");

    private Log(Name alias, Table<LogRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Log(Name alias, Table<LogRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.log</code> table reference
     */
    public Log(String alias) {
        this(DSL.name(alias), LOG);
    }

    /**
     * Create an aliased <code>oagi.log</code> table reference
     */
    public Log(Name alias) {
        this(alias, LOG);
    }

    /**
     * Create a <code>oagi.log</code> table reference
     */
    public Log() {
        this(DSL.name("log"), null);
    }

    public <O extends Record> Log(Table<O> path, ForeignKey<O, LogRecord> childPath, InverseForeignKey<O, LogRecord> parentPath) {
        super(path, childPath, parentPath, LOG);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class LogPath extends Log implements Path<LogRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> LogPath(Table<O> path, ForeignKey<O, LogRecord> childPath, InverseForeignKey<O, LogRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private LogPath(Name alias, Table<LogRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public LogPath as(String alias) {
            return new LogPath(DSL.name(alias), this);
        }

        @Override
        public LogPath as(Name alias) {
            return new LogPath(alias, this);
        }

        @Override
        public LogPath as(Table<?> alias) {
            return new LogPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.LOG_REFERENCE);
    }

    @Override
    public Identity<LogRecord, ULong> getIdentity() {
        return (Identity<LogRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<LogRecord> getPrimaryKey() {
        return Keys.KEY_LOG_PRIMARY;
    }

    @Override
    public List<ForeignKey<LogRecord, ?>> getReferences() {
        return Arrays.asList(Keys.LOG_CREATED_BY_FK, Keys.LOG_NEXT_LOG_ID_FK, Keys.LOG_PREV_LOG_ID_FK);
    }

    private transient AppUserPath _appUser;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table.
     */
    public AppUserPath appUser() {
        if (_appUser == null)
            _appUser = new AppUserPath(this, Keys.LOG_CREATED_BY_FK, null);

        return _appUser;
    }

    private transient LogPath _logNextLogIdFk;

    /**
     * Get the implicit join path to the <code>oagi.log</code> table, via the
     * <code>log_next_log_id_fk</code> key.
     */
    public LogPath logNextLogIdFk() {
        if (_logNextLogIdFk == null)
            _logNextLogIdFk = new LogPath(this, Keys.LOG_NEXT_LOG_ID_FK, null);

        return _logNextLogIdFk;
    }

    private transient LogPath _logPrevLogIdFk;

    /**
     * Get the implicit join path to the <code>oagi.log</code> table, via the
     * <code>log_prev_log_id_fk</code> key.
     */
    public LogPath logPrevLogIdFk() {
        if (_logPrevLogIdFk == null)
            _logPrevLogIdFk = new LogPath(this, Keys.LOG_PREV_LOG_ID_FK, null);

        return _logPrevLogIdFk;
    }

    private transient AccManifestPath _accManifest;

    /**
     * Get the implicit to-many join path to the <code>oagi.acc_manifest</code>
     * table
     */
    public AccManifestPath accManifest() {
        if (_accManifest == null)
            _accManifest = new AccManifestPath(this, null, Keys.ACC_MANIFEST_LOG_ID_FK.getInverseKey());

        return _accManifest;
    }

    private transient AgencyIdListManifestPath _agencyIdListManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.agency_id_list_manifest</code> table
     */
    public AgencyIdListManifestPath agencyIdListManifest() {
        if (_agencyIdListManifest == null)
            _agencyIdListManifest = new AgencyIdListManifestPath(this, null, Keys.AGENCY_ID_LIST_MANIFEST_LOG_ID_FK.getInverseKey());

        return _agencyIdListManifest;
    }

    private transient AsccpManifestPath _asccpManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.asccp_manifest</code> table
     */
    public AsccpManifestPath asccpManifest() {
        if (_asccpManifest == null)
            _asccpManifest = new AsccpManifestPath(this, null, Keys.ASCCP_MANIFEST_LOG_ID_FK.getInverseKey());

        return _asccpManifest;
    }

    private transient BccpManifestPath _bccpManifest;

    /**
     * Get the implicit to-many join path to the <code>oagi.bccp_manifest</code>
     * table
     */
    public BccpManifestPath bccpManifest() {
        if (_bccpManifest == null)
            _bccpManifest = new BccpManifestPath(this, null, Keys.BCCP_MANIFEST_LOG_ID_FK.getInverseKey());

        return _bccpManifest;
    }

    private transient CodeListManifestPath _codeListManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.code_list_manifest</code> table
     */
    public CodeListManifestPath codeListManifest() {
        if (_codeListManifest == null)
            _codeListManifest = new CodeListManifestPath(this, null, Keys.CODE_LIST_MANIFEST_LOG_ID_FK.getInverseKey());

        return _codeListManifest;
    }

    private transient DtManifestPath _dtManifest;

    /**
     * Get the implicit to-many join path to the <code>oagi.dt_manifest</code>
     * table
     */
    public DtManifestPath dtManifest() {
        if (_dtManifest == null)
            _dtManifest = new DtManifestPath(this, null, Keys.DT_MANIFEST_LOG_ID_FK.getInverseKey());

        return _dtManifest;
    }

    private transient XbtManifestPath _xbtManifest;

    /**
     * Get the implicit to-many join path to the <code>oagi.xbt_manifest</code>
     * table
     */
    public XbtManifestPath xbtManifest() {
        if (_xbtManifest == null)
            _xbtManifest = new XbtManifestPath(this, null, Keys.XBT_MANIFEST_LOG_ID_FK.getInverseKey());

        return _xbtManifest;
    }

    @Override
    public List<Check<LogRecord>> getChecks() {
        return Arrays.asList(
            Internal.createCheck(this, DSL.name("log_chk_1"), "json_valid(`snapshot`)", true)
        );
    }

    @Override
    public Log as(String alias) {
        return new Log(DSL.name(alias), this);
    }

    @Override
    public Log as(Name alias) {
        return new Log(alias, this);
    }

    @Override
    public Log as(Table<?> alias) {
        return new Log(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Log rename(String name) {
        return new Log(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Log rename(Name name) {
        return new Log(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Log rename(Table<?> name) {
        return new Log(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Log where(Condition condition) {
        return new Log(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Log where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Log where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Log where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Log where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Log where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Log where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Log where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Log whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Log whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
