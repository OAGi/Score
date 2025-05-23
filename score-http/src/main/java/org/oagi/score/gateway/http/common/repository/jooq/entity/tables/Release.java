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
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AccManifest.AccManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AgencyIdListManifest.AgencyIdListManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AgencyIdListValueManifest.AgencyIdListValueManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AppUser.AppUserPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AsccManifest.AsccManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AsccpManifest.AsccpManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BccManifest.BccManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BccpManifest.BccpManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BlobContentManifest.BlobContentManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.CodeListManifest.CodeListManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.CodeListValueManifest.CodeListValueManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.DtAwdPri.DtAwdPriPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.DtManifest.DtManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.DtScAwdPri.DtScAwdPriPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.DtScManifest.DtScManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Library.LibraryPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.ModuleSetRelease.ModuleSetReleasePath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Namespace.NamespacePath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Release.ReleasePath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.ReleaseDep.ReleaseDepPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.TopLevelAsbiep.TopLevelAsbiepPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.XbtManifest.XbtManifestPath;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.ReleaseRecord;


/**
 * The is table store the release information.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Release extends TableImpl<ReleaseRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.release</code>
     */
    public static final Release RELEASE = new Release();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ReleaseRecord> getRecordType() {
        return ReleaseRecord.class;
    }

    /**
     * The column <code>oagi.release.release_id</code>. RELEASE_ID must be an
     * incremental integer. RELEASE_ID that is more than another RELEASE_ID is
     * interpreted to be released later than the other.
     */
    public final TableField<ReleaseRecord, ULong> RELEASE_ID = createField(DSL.name("release_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "RELEASE_ID must be an incremental integer. RELEASE_ID that is more than another RELEASE_ID is interpreted to be released later than the other.");

    /**
     * The column <code>oagi.release.library_id</code>. A foreign key pointed to
     * a library of the current record.
     */
    public final TableField<ReleaseRecord, ULong> LIBRARY_ID = createField(DSL.name("library_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key pointed to a library of the current record.");

    /**
     * The column <code>oagi.release.guid</code>. A globally unique identifier
     * (GUID).
     */
    public final TableField<ReleaseRecord, String> GUID = createField(DSL.name("guid"), SQLDataType.CHAR(32).nullable(false), this, "A globally unique identifier (GUID).");

    /**
     * The column <code>oagi.release.release_num</code>. Release number such has
     * 10.0, 10.1, etc. 
     */
    public final TableField<ReleaseRecord, String> RELEASE_NUM = createField(DSL.name("release_num"), SQLDataType.VARCHAR(45).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "Release number such has 10.0, 10.1, etc. ");

    /**
     * The column <code>oagi.release.release_note</code>. Description or note
     * associated with the release.
     */
    public final TableField<ReleaseRecord, String> RELEASE_NOTE = createField(DSL.name("release_note"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "Description or note associated with the release.");

    /**
     * The column <code>oagi.release.release_license</code>. License associated
     * with the release.
     */
    public final TableField<ReleaseRecord, String> RELEASE_LICENSE = createField(DSL.name("release_license"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "License associated with the release.");

    /**
     * The column <code>oagi.release.namespace_id</code>. Foreign key to the
     * NAMESPACE table. It identifies the namespace used with the release. It is
     * particularly useful for a library that uses a single namespace such like
     * the OAGIS 10.x. A library that uses multiple namespace but has a main
     * namespace may also use this column as a specific namespace can be
     * override at the module level.
     */
    public final TableField<ReleaseRecord, ULong> NAMESPACE_ID = createField(DSL.name("namespace_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "Foreign key to the NAMESPACE table. It identifies the namespace used with the release. It is particularly useful for a library that uses a single namespace such like the OAGIS 10.x. A library that uses multiple namespace but has a main namespace may also use this column as a specific namespace can be override at the module level.");

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
    public final TableField<ReleaseRecord, String> STATE = createField(DSL.name("state"), SQLDataType.VARCHAR(20).defaultValue(DSL.field(DSL.raw("'Initialized'"), SQLDataType.VARCHAR)), this, "This indicates the revision life cycle state of the Release.");

    /**
     * The column <code>oagi.release.prev_release_id</code>. Foreign key
     * referencing the previous release record.
     */
    public final TableField<ReleaseRecord, ULong> PREV_RELEASE_ID = createField(DSL.name("prev_release_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "Foreign key referencing the previous release record.");

    /**
     * The column <code>oagi.release.next_release_id</code>. Foreign key
     * referencing the next release record.
     */
    public final TableField<ReleaseRecord, ULong> NEXT_RELEASE_ID = createField(DSL.name("next_release_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "Foreign key referencing the next release record.");

    private Release(Name alias, Table<ReleaseRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Release(Name alias, Table<ReleaseRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment("The is table store the release information."), TableOptions.table(), where);
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

    public <O extends Record> Release(Table<O> path, ForeignKey<O, ReleaseRecord> childPath, InverseForeignKey<O, ReleaseRecord> parentPath) {
        super(path, childPath, parentPath, RELEASE);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class ReleasePath extends Release implements Path<ReleaseRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> ReleasePath(Table<O> path, ForeignKey<O, ReleaseRecord> childPath, InverseForeignKey<O, ReleaseRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private ReleasePath(Name alias, Table<ReleaseRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public ReleasePath as(String alias) {
            return new ReleasePath(DSL.name(alias), this);
        }

        @Override
        public ReleasePath as(Name alias) {
            return new ReleasePath(alias, this);
        }

        @Override
        public ReleasePath as(Table<?> alias) {
            return new ReleasePath(alias.getQualifiedName(), this);
        }
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
        return Arrays.asList(Keys.RELEASE_CREATED_BY_FK, Keys.RELEASE_LAST_UPDATED_BY_FK, Keys.RELEASE_LIBRARY_ID_FK, Keys.RELEASE_NAMESPACE_ID_FK, Keys.RELEASE_NEXT_RELEASE_ID_FK, Keys.RELEASE_PREV_RELEASE_ID_FK);
    }

    private transient AppUserPath _releaseCreatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>release_created_by_fk</code> key.
     */
    public AppUserPath releaseCreatedByFk() {
        if (_releaseCreatedByFk == null)
            _releaseCreatedByFk = new AppUserPath(this, Keys.RELEASE_CREATED_BY_FK, null);

        return _releaseCreatedByFk;
    }

    private transient AppUserPath _releaseLastUpdatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>release_last_updated_by_fk</code> key.
     */
    public AppUserPath releaseLastUpdatedByFk() {
        if (_releaseLastUpdatedByFk == null)
            _releaseLastUpdatedByFk = new AppUserPath(this, Keys.RELEASE_LAST_UPDATED_BY_FK, null);

        return _releaseLastUpdatedByFk;
    }

    private transient LibraryPath _library;

    /**
     * Get the implicit join path to the <code>oagi.library</code> table.
     */
    public LibraryPath library() {
        if (_library == null)
            _library = new LibraryPath(this, Keys.RELEASE_LIBRARY_ID_FK, null);

        return _library;
    }

    private transient NamespacePath _namespace;

    /**
     * Get the implicit join path to the <code>oagi.namespace</code> table.
     */
    public NamespacePath namespace() {
        if (_namespace == null)
            _namespace = new NamespacePath(this, Keys.RELEASE_NAMESPACE_ID_FK, null);

        return _namespace;
    }

    private transient ReleasePath _releaseNextReleaseIdFk;

    /**
     * Get the implicit join path to the <code>oagi.release</code> table, via
     * the <code>release_next_release_id_fk</code> key.
     */
    public ReleasePath releaseNextReleaseIdFk() {
        if (_releaseNextReleaseIdFk == null)
            _releaseNextReleaseIdFk = new ReleasePath(this, Keys.RELEASE_NEXT_RELEASE_ID_FK, null);

        return _releaseNextReleaseIdFk;
    }

    private transient ReleasePath _releasePrevReleaseIdFk;

    /**
     * Get the implicit join path to the <code>oagi.release</code> table, via
     * the <code>release_prev_release_id_fk</code> key.
     */
    public ReleasePath releasePrevReleaseIdFk() {
        if (_releasePrevReleaseIdFk == null)
            _releasePrevReleaseIdFk = new ReleasePath(this, Keys.RELEASE_PREV_RELEASE_ID_FK, null);

        return _releasePrevReleaseIdFk;
    }

    private transient AccManifestPath _accManifest;

    /**
     * Get the implicit to-many join path to the <code>oagi.acc_manifest</code>
     * table
     */
    public AccManifestPath accManifest() {
        if (_accManifest == null)
            _accManifest = new AccManifestPath(this, null, Keys.ACC_MANIFEST_RELEASE_ID_FK.getInverseKey());

        return _accManifest;
    }

    private transient AgencyIdListManifestPath _agencyIdListManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.agency_id_list_manifest</code> table
     */
    public AgencyIdListManifestPath agencyIdListManifest() {
        if (_agencyIdListManifest == null)
            _agencyIdListManifest = new AgencyIdListManifestPath(this, null, Keys.AGENCY_ID_LIST_MANIFEST_RELEASE_ID_FK.getInverseKey());

        return _agencyIdListManifest;
    }

    private transient AgencyIdListValueManifestPath _agencyIdListValueManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.agency_id_list_value_manifest</code> table
     */
    public AgencyIdListValueManifestPath agencyIdListValueManifest() {
        if (_agencyIdListValueManifest == null)
            _agencyIdListValueManifest = new AgencyIdListValueManifestPath(this, null, Keys.AGENCY_ID_LIST_VALUE_MANIFEST_RELEASE_ID_FK.getInverseKey());

        return _agencyIdListValueManifest;
    }

    private transient AsccManifestPath _asccManifest;

    /**
     * Get the implicit to-many join path to the <code>oagi.ascc_manifest</code>
     * table
     */
    public AsccManifestPath asccManifest() {
        if (_asccManifest == null)
            _asccManifest = new AsccManifestPath(this, null, Keys.ASCC_MANIFEST_RELEASE_ID_FK.getInverseKey());

        return _asccManifest;
    }

    private transient AsccpManifestPath _asccpManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.asccp_manifest</code> table
     */
    public AsccpManifestPath asccpManifest() {
        if (_asccpManifest == null)
            _asccpManifest = new AsccpManifestPath(this, null, Keys.ASCCP_MANIFEST_RELEASE_ID_FK.getInverseKey());

        return _asccpManifest;
    }

    private transient BccManifestPath _bccManifest;

    /**
     * Get the implicit to-many join path to the <code>oagi.bcc_manifest</code>
     * table
     */
    public BccManifestPath bccManifest() {
        if (_bccManifest == null)
            _bccManifest = new BccManifestPath(this, null, Keys.BCC_MANIFEST_RELEASE_ID_FK.getInverseKey());

        return _bccManifest;
    }

    private transient BccpManifestPath _bccpManifest;

    /**
     * Get the implicit to-many join path to the <code>oagi.bccp_manifest</code>
     * table
     */
    public BccpManifestPath bccpManifest() {
        if (_bccpManifest == null)
            _bccpManifest = new BccpManifestPath(this, null, Keys.BCCP_MANIFEST_RELEASE_ID_FK.getInverseKey());

        return _bccpManifest;
    }

    private transient BlobContentManifestPath _blobContentManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.blob_content_manifest</code> table
     */
    public BlobContentManifestPath blobContentManifest() {
        if (_blobContentManifest == null)
            _blobContentManifest = new BlobContentManifestPath(this, null, Keys.BLOB_CONTENT_MANIFEST_RELEASE_ID_FK.getInverseKey());

        return _blobContentManifest;
    }

    private transient CodeListManifestPath _codeListManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.code_list_manifest</code> table
     */
    public CodeListManifestPath codeListManifest() {
        if (_codeListManifest == null)
            _codeListManifest = new CodeListManifestPath(this, null, Keys.CODE_LIST_MANIFEST_RELEASE_ID_FK.getInverseKey());

        return _codeListManifest;
    }

    private transient CodeListValueManifestPath _codeListValueManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.code_list_value_manifest</code> table
     */
    public CodeListValueManifestPath codeListValueManifest() {
        if (_codeListValueManifest == null)
            _codeListValueManifest = new CodeListValueManifestPath(this, null, Keys.CODE_LIST_VALUE_MANIFEST_RELEASE_ID_FK.getInverseKey());

        return _codeListValueManifest;
    }

    private transient DtAwdPriPath _dtAwdPri;

    /**
     * Get the implicit to-many join path to the <code>oagi.dt_awd_pri</code>
     * table
     */
    public DtAwdPriPath dtAwdPri() {
        if (_dtAwdPri == null)
            _dtAwdPri = new DtAwdPriPath(this, null, Keys.DT_AWD_PRI_RELEASE_ID_FK.getInverseKey());

        return _dtAwdPri;
    }

    private transient DtManifestPath _dtManifest;

    /**
     * Get the implicit to-many join path to the <code>oagi.dt_manifest</code>
     * table
     */
    public DtManifestPath dtManifest() {
        if (_dtManifest == null)
            _dtManifest = new DtManifestPath(this, null, Keys.DT_MANIFEST_RELEASE_ID_FK.getInverseKey());

        return _dtManifest;
    }

    private transient DtScAwdPriPath _dtScAwdPri;

    /**
     * Get the implicit to-many join path to the <code>oagi.dt_sc_awd_pri</code>
     * table
     */
    public DtScAwdPriPath dtScAwdPri() {
        if (_dtScAwdPri == null)
            _dtScAwdPri = new DtScAwdPriPath(this, null, Keys.DT_SC_AWD_PRI_RELEASE_ID_FK.getInverseKey());

        return _dtScAwdPri;
    }

    private transient DtScManifestPath _dtScManifest;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.dt_sc_manifest</code> table
     */
    public DtScManifestPath dtScManifest() {
        if (_dtScManifest == null)
            _dtScManifest = new DtScManifestPath(this, null, Keys.DT_SC_MANIFEST_RELEASE_ID_FK.getInverseKey());

        return _dtScManifest;
    }

    private transient ModuleSetReleasePath _moduleSetRelease;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.module_set_release</code> table
     */
    public ModuleSetReleasePath moduleSetRelease() {
        if (_moduleSetRelease == null)
            _moduleSetRelease = new ModuleSetReleasePath(this, null, Keys.MODULE_SET_RELEASE_RELEASE_ID_FK.getInverseKey());

        return _moduleSetRelease;
    }

    private transient ReleaseDepPath _releaseDepDependOnReleaseIdFk;

    /**
     * Get the implicit to-many join path to the <code>oagi.release_dep</code>
     * table, via the <code>release_dep_depend_on_release_id_fk</code> key
     */
    public ReleaseDepPath releaseDepDependOnReleaseIdFk() {
        if (_releaseDepDependOnReleaseIdFk == null)
            _releaseDepDependOnReleaseIdFk = new ReleaseDepPath(this, null, Keys.RELEASE_DEP_DEPEND_ON_RELEASE_ID_FK.getInverseKey());

        return _releaseDepDependOnReleaseIdFk;
    }

    private transient ReleaseDepPath _releaseDepReleaseIdFk;

    /**
     * Get the implicit to-many join path to the <code>oagi.release_dep</code>
     * table, via the <code>release_dep_release_id_fk</code> key
     */
    public ReleaseDepPath releaseDepReleaseIdFk() {
        if (_releaseDepReleaseIdFk == null)
            _releaseDepReleaseIdFk = new ReleaseDepPath(this, null, Keys.RELEASE_DEP_RELEASE_ID_FK.getInverseKey());

        return _releaseDepReleaseIdFk;
    }

    private transient TopLevelAsbiepPath _topLevelAsbiep;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.top_level_asbiep</code> table
     */
    public TopLevelAsbiepPath topLevelAsbiep() {
        if (_topLevelAsbiep == null)
            _topLevelAsbiep = new TopLevelAsbiepPath(this, null, Keys.TOP_LEVEL_ASBIEP_RELEASE_ID_FK.getInverseKey());

        return _topLevelAsbiep;
    }

    private transient XbtManifestPath _xbtManifest;

    /**
     * Get the implicit to-many join path to the <code>oagi.xbt_manifest</code>
     * table
     */
    public XbtManifestPath xbtManifest() {
        if (_xbtManifest == null)
            _xbtManifest = new XbtManifestPath(this, null, Keys.XBT_MANIFEST_RELEASE_ID_FK.getInverseKey());

        return _xbtManifest;
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

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Release where(Condition condition) {
        return new Release(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Release where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Release where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Release where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Release where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Release where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Release where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Release where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Release whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Release whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
