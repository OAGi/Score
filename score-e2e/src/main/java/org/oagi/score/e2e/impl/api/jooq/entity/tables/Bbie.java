/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables;


import java.math.BigDecimal;
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
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Abie.AbiePath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AgencyIdListManifest.AgencyIdListManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AppUser.AppUserPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.BbieBizterm.BbieBiztermPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.BbieSc.BbieScPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Bbiep.BbiepPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.BccManifest.BccManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.BieUsageRule.BieUsageRulePath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.CodeListManifest.CodeListManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.TopLevelAsbiep.TopLevelAsbiepPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.XbtManifest.XbtManifestPath;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.BbieRecord;


/**
 * A BBIE represents a relationship/association between an ABIE and a BBIEP. It
 * is a contextualization of a BCC. The BBIE table also stores some information
 * about the specific constraints related to the BDT associated with the BBIEP.
 * In particular, the three columns including the BDT_PRI_RESTRI_ID,
 * CODE_LIST_ID, and AGENCY_ID_LIST_ID allows for capturing of the specific
 * primitive to be used in the context. Only one column among the three can have
 * a value in a particular record.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Bbie extends TableImpl<BbieRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.bbie</code>
     */
    public static final Bbie BBIE = new Bbie();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BbieRecord> getRecordType() {
        return BbieRecord.class;
    }

    /**
     * The column <code>oagi.bbie.bbie_id</code>. A internal, primary database
     * key of a BBIE.
     */
    public final TableField<BbieRecord, ULong> BBIE_ID = createField(DSL.name("bbie_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "A internal, primary database key of a BBIE.");

    /**
     * The column <code>oagi.bbie.guid</code>. A globally unique identifier
     * (GUID).
     */
    public final TableField<BbieRecord, String> GUID = createField(DSL.name("guid"), SQLDataType.CHAR(32).nullable(false), this, "A globally unique identifier (GUID).");

    /**
     * The column <code>oagi.bbie.based_bcc_manifest_id</code>. The
     * BASED_BCC_MANIFEST_ID column refers to the BCC_MANIFEST record, which
     * this BBIE contextualizes.
     */
    public final TableField<BbieRecord, ULong> BASED_BCC_MANIFEST_ID = createField(DSL.name("based_bcc_manifest_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "The BASED_BCC_MANIFEST_ID column refers to the BCC_MANIFEST record, which this BBIE contextualizes.");

    /**
     * The column <code>oagi.bbie.path</code>.
     */
    public final TableField<BbieRecord, String> PATH = createField(DSL.name("path"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "");

    /**
     * The column <code>oagi.bbie.hash_path</code>. hash_path generated from the
     * path of the component graph using hash function, so that it is unique in
     * the graph.
     */
    public final TableField<BbieRecord, String> HASH_PATH = createField(DSL.name("hash_path"), SQLDataType.VARCHAR(64).nullable(false), this, "hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.");

    /**
     * The column <code>oagi.bbie.from_abie_id</code>. FROM_ABIE_ID must be
     * based on the FROM_ACC_ID in the BASED_BCC_ID.
     */
    public final TableField<BbieRecord, ULong> FROM_ABIE_ID = createField(DSL.name("from_abie_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "FROM_ABIE_ID must be based on the FROM_ACC_ID in the BASED_BCC_ID.");

    /**
     * The column <code>oagi.bbie.to_bbiep_id</code>. TO_BBIEP_ID is a foreign
     * key to the BBIEP table. TO_BBIEP_ID basically refers to a child data
     * element of the FROM_ABIE_ID. TO_BBIEP_ID must be based on the TO_BCCP_ID
     * in the based BCC.
     */
    public final TableField<BbieRecord, ULong> TO_BBIEP_ID = createField(DSL.name("to_bbiep_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "TO_BBIEP_ID is a foreign key to the BBIEP table. TO_BBIEP_ID basically refers to a child data element of the FROM_ABIE_ID. TO_BBIEP_ID must be based on the TO_BCCP_ID in the based BCC.");

    /**
     * The column <code>oagi.bbie.xbt_manifest_id</code>. This is the foreign
     * key to the XBT_MANIFEST table. It indicates the primitive assigned to the
     * BBIE (or also can be viewed as assigned to the BBIEP for this specific
     * association). This is assigned by the user who authors the BIE. The
     * assignment would override the default from the DT_AWD_PRI side.
     */
    public final TableField<BbieRecord, ULong> XBT_MANIFEST_ID = createField(DSL.name("xbt_manifest_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "This is the foreign key to the XBT_MANIFEST table. It indicates the primitive assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this specific association). This is assigned by the user who authors the BIE. The assignment would override the default from the DT_AWD_PRI side.");

    /**
     * The column <code>oagi.bbie.code_list_manifest_id</code>. This is a
     * foreign key to the CODE_LIST_MANIFEST table. If a code list is assigned
     * to the BBIE (or also can be viewed as assigned to the BBIEP for this
     * association), then this column stores the assigned code list. It should
     * be noted that one of the possible primitives assignable to the
     * BDT_PRI_RESTRI_ID column may also be a code list. So this column is
     * typically used when the user wants to assign another code list different
     * from the one permissible by the CC model.
     */
    public final TableField<BbieRecord, ULong> CODE_LIST_MANIFEST_ID = createField(DSL.name("code_list_manifest_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "This is a foreign key to the CODE_LIST_MANIFEST table. If a code list is assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this association), then this column stores the assigned code list. It should be noted that one of the possible primitives assignable to the BDT_PRI_RESTRI_ID column may also be a code list. So this column is typically used when the user wants to assign another code list different from the one permissible by the CC model.");

    /**
     * The column <code>oagi.bbie.agency_id_list_manifest_id</code>. This is a
     * foreign key to the AGENCY_ID_LIST_MANIFEST table. It is used in the case
     * that the BDT content can be restricted to an agency identification.
     */
    public final TableField<BbieRecord, ULong> AGENCY_ID_LIST_MANIFEST_ID = createField(DSL.name("agency_id_list_manifest_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "This is a foreign key to the AGENCY_ID_LIST_MANIFEST table. It is used in the case that the BDT content can be restricted to an agency identification.");

    /**
     * The column <code>oagi.bbie.cardinality_min</code>. The minimum occurrence
     * constraint for the BBIE. A valid value is a non-negative integer.
     */
    public final TableField<BbieRecord, Integer> CARDINALITY_MIN = createField(DSL.name("cardinality_min"), SQLDataType.INTEGER.nullable(false), this, "The minimum occurrence constraint for the BBIE. A valid value is a non-negative integer.");

    /**
     * The column <code>oagi.bbie.cardinality_max</code>. Maximum occurence
     * constraint of the TO_BBIEP_ID. A valid value is an integer from -1 and
     * up. Specifically, -1 means unbounded. 0 means prohibited or not to use.
     */
    public final TableField<BbieRecord, Integer> CARDINALITY_MAX = createField(DSL.name("cardinality_max"), SQLDataType.INTEGER.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.INTEGER)), this, "Maximum occurence constraint of the TO_BBIEP_ID. A valid value is an integer from -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.");

    /**
     * The column <code>oagi.bbie.facet_min_length</code>. Defines the minimum
     * number of units of length.
     */
    public final TableField<BbieRecord, ULong> FACET_MIN_LENGTH = createField(DSL.name("facet_min_length"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "Defines the minimum number of units of length.");

    /**
     * The column <code>oagi.bbie.facet_max_length</code>. Defines the minimum
     * number of units of length.
     */
    public final TableField<BbieRecord, ULong> FACET_MAX_LENGTH = createField(DSL.name("facet_max_length"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "Defines the minimum number of units of length.");

    /**
     * The column <code>oagi.bbie.facet_pattern</code>. Defines a constraint on
     * the lexical space of a datatype to literals in a specific pattern.
     */
    public final TableField<BbieRecord, String> FACET_PATTERN = createField(DSL.name("facet_pattern"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "Defines a constraint on the lexical space of a datatype to literals in a specific pattern.");

    /**
     * The column <code>oagi.bbie.default_value</code>. This column specifies
     * the default value constraint. Default and fixed value constraints cannot
     * be used at the same time.
     */
    public final TableField<BbieRecord, String> DEFAULT_VALUE = createField(DSL.name("default_value"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "This column specifies the default value constraint. Default and fixed value constraints cannot be used at the same time.");

    /**
     * The column <code>oagi.bbie.is_nillable</code>. Indicate whether the field
     * can have a null  This is corresponding to the nillable flag in the XML
     * schema.
     */
    public final TableField<BbieRecord, Byte> IS_NILLABLE = createField(DSL.name("is_nillable"), SQLDataType.TINYINT.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.TINYINT)), this, "Indicate whether the field can have a null  This is corresponding to the nillable flag in the XML schema.");

    /**
     * The column <code>oagi.bbie.fixed_value</code>. This column captures the
     * fixed value constraint. Default and fixed value constraints cannot be
     * used at the same time.
     */
    public final TableField<BbieRecord, String> FIXED_VALUE = createField(DSL.name("fixed_value"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "This column captures the fixed value constraint. Default and fixed value constraints cannot be used at the same time.");

    /**
     * The column <code>oagi.bbie.is_null</code>. This column indicates whether
     * the field is fixed to NULL. IS_NULLl can be true only if the IS_NILLABLE
     * is true. If IS_NULL is true then the FIX_VALUE and DEFAULT_VALUE columns
     * cannot have a value.
     */
    public final TableField<BbieRecord, Byte> IS_NULL = createField(DSL.name("is_null"), SQLDataType.TINYINT.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.TINYINT)), this, "This column indicates whether the field is fixed to NULL. IS_NULLl can be true only if the IS_NILLABLE is true. If IS_NULL is true then the FIX_VALUE and DEFAULT_VALUE columns cannot have a value.");

    /**
     * The column <code>oagi.bbie.definition</code>. Description to override the
     * BCC definition. If NULLl, it means that the definition should be
     * inherited from the based BCC.
     */
    public final TableField<BbieRecord, String> DEFINITION = createField(DSL.name("definition"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "Description to override the BCC definition. If NULLl, it means that the definition should be inherited from the based BCC.");

    /**
     * The column <code>oagi.bbie.example</code>.
     */
    public final TableField<BbieRecord, String> EXAMPLE = createField(DSL.name("example"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "");

    /**
     * The column <code>oagi.bbie.remark</code>. This column allows the user to
     * specify very context-specific usage of the BIE. It is different from the
     * DEFINITION column in that the DEFINITION column is a description
     * conveying the meaning of the associated concept. Remarks may be a very
     * implementation specific instruction or others. For example, BOM BOD, as
     * an ACC, is a generic BOM structure. In a particular context, a BOM ABIE
     * can be a Super BOM. Explanation of the Super BOM concept should be
     * captured in the Definition of the ABIE. A remark about that ABIE may be
     * "Type of BOM should be recognized in the BOM/typeCode."
     */
    public final TableField<BbieRecord, String> REMARK = createField(DSL.name("remark"), SQLDataType.VARCHAR(225).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "This column allows the user to specify very context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be \"Type of BOM should be recognized in the BOM/typeCode.\"");

    /**
     * The column <code>oagi.bbie.created_by</code>. A foreign key referring to
     * the user who creates the BBIE. The creator of the BBIE is also its owner
     * by default. BBIEs created as children of another ABIE have the same
     * CREATED_BY.
     */
    public final TableField<BbieRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key referring to the user who creates the BBIE. The creator of the BBIE is also its owner by default. BBIEs created as children of another ABIE have the same CREATED_BY.");

    /**
     * The column <code>oagi.bbie.last_updated_by</code>. A foreign key
     * referring to the user who has last updated the ASBIE record. 
     */
    public final TableField<BbieRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "A foreign key referring to the user who has last updated the ASBIE record. ");

    /**
     * The column <code>oagi.bbie.creation_timestamp</code>. Timestamp when the
     * BBIE record was first created. BBIEs created as children of another ABIE
     * have the same CREATION_TIMESTAMP.
     */
    public final TableField<BbieRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "Timestamp when the BBIE record was first created. BBIEs created as children of another ABIE have the same CREATION_TIMESTAMP.");

    /**
     * The column <code>oagi.bbie.last_update_timestamp</code>. The timestamp
     * when the ASBIE was last updated.
     */
    public final TableField<BbieRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "The timestamp when the ASBIE was last updated.");

    /**
     * The column <code>oagi.bbie.seq_key</code>. This indicates the order of
     * the associations among other siblings. The SEQ_KEY for BIEs is decimal in
     * order to accomodate the removal of inheritance hierarchy and group. For
     * example, children of the most abstract ACC will have SEQ_KEY = 1.1, 1.2,
     * 1.3, and so on; and SEQ_KEY of the next abstraction level ACC will have
     * SEQ_KEY = 2.1, 2.2, 2.3 and so on so forth.
     */
    public final TableField<BbieRecord, BigDecimal> SEQ_KEY = createField(DSL.name("seq_key"), SQLDataType.DECIMAL(10, 2).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.DECIMAL)), this, "This indicates the order of the associations among other siblings. The SEQ_KEY for BIEs is decimal in order to accomodate the removal of inheritance hierarchy and group. For example, children of the most abstract ACC will have SEQ_KEY = 1.1, 1.2, 1.3, and so on; and SEQ_KEY of the next abstraction level ACC will have SEQ_KEY = 2.1, 2.2, 2.3 and so on so forth.");

    /**
     * The column <code>oagi.bbie.is_used</code>. Flag to indicate whether the
     * field/component is used in the content model. It indicates whether the
     * field/component should be generated in the expression generation.
     */
    public final TableField<BbieRecord, Byte> IS_USED = createField(DSL.name("is_used"), SQLDataType.TINYINT.defaultValue(DSL.field(DSL.raw("0"), SQLDataType.TINYINT)), this, "Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated in the expression generation.");

    /**
     * The column <code>oagi.bbie.is_deprecated</code>. Indicates whether the
     * BBIE is deprecated.
     */
    public final TableField<BbieRecord, Byte> IS_DEPRECATED = createField(DSL.name("is_deprecated"), SQLDataType.TINYINT.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.TINYINT)), this, "Indicates whether the BBIE is deprecated.");

    /**
     * The column <code>oagi.bbie.owner_top_level_asbiep_id</code>. This is a
     * foreign key to the top-level ASBIEP.
     */
    public final TableField<BbieRecord, ULong> OWNER_TOP_LEVEL_ASBIEP_ID = createField(DSL.name("owner_top_level_asbiep_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "This is a foreign key to the top-level ASBIEP.");

    private Bbie(Name alias, Table<BbieRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Bbie(Name alias, Table<BbieRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment("A BBIE represents a relationship/association between an ABIE and a BBIEP. It is a contextualization of a BCC. The BBIE table also stores some information about the specific constraints related to the BDT associated with the BBIEP. In particular, the three columns including the BDT_PRI_RESTRI_ID, CODE_LIST_ID, and AGENCY_ID_LIST_ID allows for capturing of the specific primitive to be used in the context. Only one column among the three can have a value in a particular record."), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>oagi.bbie</code> table reference
     */
    public Bbie(String alias) {
        this(DSL.name(alias), BBIE);
    }

    /**
     * Create an aliased <code>oagi.bbie</code> table reference
     */
    public Bbie(Name alias) {
        this(alias, BBIE);
    }

    /**
     * Create a <code>oagi.bbie</code> table reference
     */
    public Bbie() {
        this(DSL.name("bbie"), null);
    }

    public <O extends Record> Bbie(Table<O> path, ForeignKey<O, BbieRecord> childPath, InverseForeignKey<O, BbieRecord> parentPath) {
        super(path, childPath, parentPath, BBIE);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class BbiePath extends Bbie implements Path<BbieRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> BbiePath(Table<O> path, ForeignKey<O, BbieRecord> childPath, InverseForeignKey<O, BbieRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private BbiePath(Name alias, Table<BbieRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public BbiePath as(String alias) {
            return new BbiePath(DSL.name(alias), this);
        }

        @Override
        public BbiePath as(Name alias) {
            return new BbiePath(alias, this);
        }

        @Override
        public BbiePath as(Table<?> alias) {
            return new BbiePath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.BBIE_BBIE_HASH_PATH_K, Indexes.BBIE_BBIE_PATH_K);
    }

    @Override
    public Identity<BbieRecord, ULong> getIdentity() {
        return (Identity<BbieRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<BbieRecord> getPrimaryKey() {
        return Keys.KEY_BBIE_PRIMARY;
    }

    @Override
    public List<ForeignKey<BbieRecord, ?>> getReferences() {
        return Arrays.asList(Keys.BBIE_AGENCY_ID_LIST_MANIFEST_ID_FK, Keys.BBIE_BASED_BCC_MANIFEST_ID_FK, Keys.BBIE_CODE_LIST_MANIFEST_ID_FK, Keys.BBIE_CREATED_BY_FK, Keys.BBIE_FROM_ABIE_ID_FK, Keys.BBIE_LAST_UPDATED_BY_FK, Keys.BBIE_OWNER_TOP_LEVEL_ASBIEP_ID_FK, Keys.BBIE_TO_BBIEP_ID_FK, Keys.BBIE_XBT_MANIFEST_ID_FK);
    }

    private transient AgencyIdListManifestPath _agencyIdListManifest;

    /**
     * Get the implicit join path to the
     * <code>oagi.agency_id_list_manifest</code> table.
     */
    public AgencyIdListManifestPath agencyIdListManifest() {
        if (_agencyIdListManifest == null)
            _agencyIdListManifest = new AgencyIdListManifestPath(this, Keys.BBIE_AGENCY_ID_LIST_MANIFEST_ID_FK, null);

        return _agencyIdListManifest;
    }

    private transient BccManifestPath _bccManifest;

    /**
     * Get the implicit join path to the <code>oagi.bcc_manifest</code> table.
     */
    public BccManifestPath bccManifest() {
        if (_bccManifest == null)
            _bccManifest = new BccManifestPath(this, Keys.BBIE_BASED_BCC_MANIFEST_ID_FK, null);

        return _bccManifest;
    }

    private transient CodeListManifestPath _codeListManifest;

    /**
     * Get the implicit join path to the <code>oagi.code_list_manifest</code>
     * table.
     */
    public CodeListManifestPath codeListManifest() {
        if (_codeListManifest == null)
            _codeListManifest = new CodeListManifestPath(this, Keys.BBIE_CODE_LIST_MANIFEST_ID_FK, null);

        return _codeListManifest;
    }

    private transient AppUserPath _bbieCreatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>bbie_created_by_fk</code> key.
     */
    public AppUserPath bbieCreatedByFk() {
        if (_bbieCreatedByFk == null)
            _bbieCreatedByFk = new AppUserPath(this, Keys.BBIE_CREATED_BY_FK, null);

        return _bbieCreatedByFk;
    }

    private transient AbiePath _abie;

    /**
     * Get the implicit join path to the <code>oagi.abie</code> table.
     */
    public AbiePath abie() {
        if (_abie == null)
            _abie = new AbiePath(this, Keys.BBIE_FROM_ABIE_ID_FK, null);

        return _abie;
    }

    private transient AppUserPath _bbieLastUpdatedByFk;

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>bbie_last_updated_by_fk</code> key.
     */
    public AppUserPath bbieLastUpdatedByFk() {
        if (_bbieLastUpdatedByFk == null)
            _bbieLastUpdatedByFk = new AppUserPath(this, Keys.BBIE_LAST_UPDATED_BY_FK, null);

        return _bbieLastUpdatedByFk;
    }

    private transient TopLevelAsbiepPath _topLevelAsbiep;

    /**
     * Get the implicit join path to the <code>oagi.top_level_asbiep</code>
     * table.
     */
    public TopLevelAsbiepPath topLevelAsbiep() {
        if (_topLevelAsbiep == null)
            _topLevelAsbiep = new TopLevelAsbiepPath(this, Keys.BBIE_OWNER_TOP_LEVEL_ASBIEP_ID_FK, null);

        return _topLevelAsbiep;
    }

    private transient BbiepPath _bbiep;

    /**
     * Get the implicit join path to the <code>oagi.bbiep</code> table.
     */
    public BbiepPath bbiep() {
        if (_bbiep == null)
            _bbiep = new BbiepPath(this, Keys.BBIE_TO_BBIEP_ID_FK, null);

        return _bbiep;
    }

    private transient XbtManifestPath _xbtManifest;

    /**
     * Get the implicit join path to the <code>oagi.xbt_manifest</code> table.
     */
    public XbtManifestPath xbtManifest() {
        if (_xbtManifest == null)
            _xbtManifest = new XbtManifestPath(this, Keys.BBIE_XBT_MANIFEST_ID_FK, null);

        return _xbtManifest;
    }

    private transient BbieBiztermPath _bbieBizterm;

    /**
     * Get the implicit to-many join path to the <code>oagi.bbie_bizterm</code>
     * table
     */
    public BbieBiztermPath bbieBizterm() {
        if (_bbieBizterm == null)
            _bbieBizterm = new BbieBiztermPath(this, null, Keys.BBIE_BIZTERM_BBIE_FK.getInverseKey());

        return _bbieBizterm;
    }

    private transient BbieScPath _bbieSc;

    /**
     * Get the implicit to-many join path to the <code>oagi.bbie_sc</code> table
     */
    public BbieScPath bbieSc() {
        if (_bbieSc == null)
            _bbieSc = new BbieScPath(this, null, Keys.BBIE_SC_BBIE_ID_FK.getInverseKey());

        return _bbieSc;
    }

    private transient BieUsageRulePath _bieUsageRule;

    /**
     * Get the implicit to-many join path to the
     * <code>oagi.bie_usage_rule</code> table
     */
    public BieUsageRulePath bieUsageRule() {
        if (_bieUsageRule == null)
            _bieUsageRule = new BieUsageRulePath(this, null, Keys.BIE_USAGE_RULE_TARGET_BBIE_ID_FK.getInverseKey());

        return _bieUsageRule;
    }

    @Override
    public Bbie as(String alias) {
        return new Bbie(DSL.name(alias), this);
    }

    @Override
    public Bbie as(Name alias) {
        return new Bbie(alias, this);
    }

    @Override
    public Bbie as(Table<?> alias) {
        return new Bbie(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Bbie rename(String name) {
        return new Bbie(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Bbie rename(Name name) {
        return new Bbie(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Bbie rename(Table<?> name) {
        return new Bbie(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Bbie where(Condition condition) {
        return new Bbie(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Bbie where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Bbie where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Bbie where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Bbie where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Bbie where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Bbie where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Bbie where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Bbie whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Bbie whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
