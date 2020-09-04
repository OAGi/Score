/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.entity.jooq.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row6;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.jooq.types.ULong;
import org.oagi.score.entity.jooq.Indexes;
import org.oagi.score.entity.jooq.Keys;
import org.oagi.score.entity.jooq.Oagi;
import org.oagi.score.entity.jooq.tables.records.BieUserExtRevisionRecord;


/**
 * This table is a log of events. It keeps track of the User Extension ACC 
 * (the specific revision) used by an Extension ABIE. This can be a named 
 * extension (such as ApplicationAreaExtension) or the AllExtension. The REVISED_INDICATOR 
 * flag is designed such that a revision of a User Extension can notify the 
 * user of a top-level ABIE by setting this flag to true. The TOP_LEVEL_ABIE_ID 
 * column makes it more efficient to when opening a top-level ABIE, the user 
 * can be notified of any new revision of the extension. A record in this 
 * table is created only when there is a user extension to the the OAGIS extension 
 * component/ACC.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.4"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BieUserExtRevision extends TableImpl<BieUserExtRevisionRecord> {

    private static final long serialVersionUID = -955602518;

    /**
     * The reference instance of <code>oagi.bie_user_ext_revision</code>
     */
    public static final BieUserExtRevision BIE_USER_EXT_REVISION = new BieUserExtRevision();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BieUserExtRevisionRecord> getRecordType() {
        return BieUserExtRevisionRecord.class;
    }

    /**
     * The column <code>oagi.bie_user_ext_revision.bie_user_ext_revision_id</code>. Primary, internal database key.
     */
    public final TableField<BieUserExtRevisionRecord, ULong> BIE_USER_EXT_REVISION_ID = createField(DSL.name("bie_user_ext_revision_id"), org.jooq.impl.SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "Primary, internal database key.");

    /**
     * The column <code>oagi.bie_user_ext_revision.ext_abie_id</code>. This points to an ABIE record corresponding to the EXTENSION_ACC_ID record. For example, this column can point to the ApplicationAreaExtension ABIE which is based on the ApplicationAreaExtension ACC (referred to by the EXT_ACC_ID column). This column can be NULL only when the extension is the AllExtension because there is no corresponding ABIE for the AllExtension ACC.
     */
    public final TableField<BieUserExtRevisionRecord, ULong> EXT_ABIE_ID = createField(DSL.name("ext_abie_id"), org.jooq.impl.SQLDataType.BIGINTUNSIGNED, this, "This points to an ABIE record corresponding to the EXTENSION_ACC_ID record. For example, this column can point to the ApplicationAreaExtension ABIE which is based on the ApplicationAreaExtension ACC (referred to by the EXT_ACC_ID column). This column can be NULL only when the extension is the AllExtension because there is no corresponding ABIE for the AllExtension ACC.");

    /**
     * The column <code>oagi.bie_user_ext_revision.ext_acc_id</code>. This points to an extension ACC on which the ABIE indicated by the EXT_ABIE_ID column is based. E.g. It may point to an ApplicationAreaExtension ACC, AllExtension ACC, ActualLedgerExtension ACC, etc. It should be noted that an ACC record pointed to must have the OAGIS_COMPONENT_TYPE = 2 (Extension).
     */
    public final TableField<BieUserExtRevisionRecord, ULong> EXT_ACC_ID = createField(DSL.name("ext_acc_id"), org.jooq.impl.SQLDataType.BIGINTUNSIGNED.nullable(false), this, "This points to an extension ACC on which the ABIE indicated by the EXT_ABIE_ID column is based. E.g. It may point to an ApplicationAreaExtension ACC, AllExtension ACC, ActualLedgerExtension ACC, etc. It should be noted that an ACC record pointed to must have the OAGIS_COMPONENT_TYPE = 2 (Extension).");

    /**
     * The column <code>oagi.bie_user_ext_revision.user_ext_acc_id</code>. This column points to the specific revision of a User Extension ACC (this is an ACC whose OAGIS_COMPONENT_TYPE = 4) currently used by the ABIE as indicated by the EXT_ABIE_ID or the by the TOP_LEVEL_ABIE_ID (in case of the AllExtension). 
     */
    public final TableField<BieUserExtRevisionRecord, ULong> USER_EXT_ACC_ID = createField(DSL.name("user_ext_acc_id"), org.jooq.impl.SQLDataType.BIGINTUNSIGNED.nullable(false), this, "This column points to the specific revision of a User Extension ACC (this is an ACC whose OAGIS_COMPONENT_TYPE = 4) currently used by the ABIE as indicated by the EXT_ABIE_ID or the by the TOP_LEVEL_ABIE_ID (in case of the AllExtension). ");

    /**
     * The column <code>oagi.bie_user_ext_revision.revised_indicator</code>. This column is a flag indicating to whether the User Extension ACC (as identified in the USER_EXT_ACC_ID column) has been revised, i.e., there is a newer version of the user extension ACC than the one currently used by the EXT_ABIE_ID. 0 means the USER_EXT_ACC_ID is current, 1 means it is not current.
     */
    public final TableField<BieUserExtRevisionRecord, Byte> REVISED_INDICATOR = createField(DSL.name("revised_indicator"), org.jooq.impl.SQLDataType.TINYINT.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.TINYINT)), this, "This column is a flag indicating to whether the User Extension ACC (as identified in the USER_EXT_ACC_ID column) has been revised, i.e., there is a newer version of the user extension ACC than the one currently used by the EXT_ABIE_ID. 0 means the USER_EXT_ACC_ID is current, 1 means it is not current.");

    /**
     * The column <code>oagi.bie_user_ext_revision.top_level_asbiep_id</code>. This is a foreign key to the top-level ASBIEP.
     */
    public final TableField<BieUserExtRevisionRecord, ULong> TOP_LEVEL_ASBIEP_ID = createField(DSL.name("top_level_asbiep_id"), org.jooq.impl.SQLDataType.BIGINTUNSIGNED.nullable(false), this, "This is a foreign key to the top-level ASBIEP.");

    /**
     * Create a <code>oagi.bie_user_ext_revision</code> table reference
     */
    public BieUserExtRevision() {
        this(DSL.name("bie_user_ext_revision"), null);
    }

    /**
     * Create an aliased <code>oagi.bie_user_ext_revision</code> table reference
     */
    public BieUserExtRevision(String alias) {
        this(DSL.name(alias), BIE_USER_EXT_REVISION);
    }

    /**
     * Create an aliased <code>oagi.bie_user_ext_revision</code> table reference
     */
    public BieUserExtRevision(Name alias) {
        this(alias, BIE_USER_EXT_REVISION);
    }

    private BieUserExtRevision(Name alias, Table<BieUserExtRevisionRecord> aliased) {
        this(alias, aliased, null);
    }

    private BieUserExtRevision(Name alias, Table<BieUserExtRevisionRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("This table is a log of events. It keeps track of the User Extension ACC (the specific revision) used by an Extension ABIE. This can be a named extension (such as ApplicationAreaExtension) or the AllExtension. The REVISED_INDICATOR flag is designed such that a revision of a User Extension can notify the user of a top-level ABIE by setting this flag to true. The TOP_LEVEL_ABIE_ID column makes it more efficient to when opening a top-level ABIE, the user can be notified of any new revision of the extension. A record in this table is created only when there is a user extension to the the OAGIS extension component/ACC."));
    }

    public <O extends Record> BieUserExtRevision(Table<O> child, ForeignKey<O, BieUserExtRevisionRecord> key) {
        super(child, key, BIE_USER_EXT_REVISION);
    }

    @Override
    public Schema getSchema() {
        return Oagi.OAGI;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.BIE_USER_EXT_REVISION_BIE_USER_EXT_REVISION_EXT_ABIE_ID_FK, Indexes.BIE_USER_EXT_REVISION_BIE_USER_EXT_REVISION_EXT_ACC_ID_FK, Indexes.BIE_USER_EXT_REVISION_BIE_USER_EXT_REVISION_TOP_LEVEL_ASBIEP_ID_FK, Indexes.BIE_USER_EXT_REVISION_BIE_USER_EXT_REVISION_USER_EXT_ACC_ID_FK, Indexes.BIE_USER_EXT_REVISION_PRIMARY);
    }

    @Override
    public Identity<BieUserExtRevisionRecord, ULong> getIdentity() {
        return Keys.IDENTITY_BIE_USER_EXT_REVISION;
    }

    @Override
    public UniqueKey<BieUserExtRevisionRecord> getPrimaryKey() {
        return Keys.KEY_BIE_USER_EXT_REVISION_PRIMARY;
    }

    @Override
    public List<UniqueKey<BieUserExtRevisionRecord>> getKeys() {
        return Arrays.<UniqueKey<BieUserExtRevisionRecord>>asList(Keys.KEY_BIE_USER_EXT_REVISION_PRIMARY);
    }

    @Override
    public List<ForeignKey<BieUserExtRevisionRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<BieUserExtRevisionRecord, ?>>asList(Keys.BIE_USER_EXT_REVISION_EXT_ABIE_ID_FK, Keys.BIE_USER_EXT_REVISION_EXT_ACC_ID_FK, Keys.BIE_USER_EXT_REVISION_USER_EXT_ACC_ID_FK, Keys.BIE_USER_EXT_REVISION_TOP_LEVEL_ASBIEP_ID_FK);
    }

    public Abie abie() {
        return new Abie(this, Keys.BIE_USER_EXT_REVISION_EXT_ABIE_ID_FK);
    }

    public Acc bieUserExtRevisionExtAccIdFk() {
        return new Acc(this, Keys.BIE_USER_EXT_REVISION_EXT_ACC_ID_FK);
    }

    public Acc bieUserExtRevisionUserExtAccIdFk() {
        return new Acc(this, Keys.BIE_USER_EXT_REVISION_USER_EXT_ACC_ID_FK);
    }

    public TopLevelAsbiep topLevelAsbiep() {
        return new TopLevelAsbiep(this, Keys.BIE_USER_EXT_REVISION_TOP_LEVEL_ASBIEP_ID_FK);
    }

    @Override
    public BieUserExtRevision as(String alias) {
        return new BieUserExtRevision(DSL.name(alias), this);
    }

    @Override
    public BieUserExtRevision as(Name alias) {
        return new BieUserExtRevision(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public BieUserExtRevision rename(String name) {
        return new BieUserExtRevision(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public BieUserExtRevision rename(Name name) {
        return new BieUserExtRevision(name, null);
    }

    // -------------------------------------------------------------------------
    // Row6 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row6<ULong, ULong, ULong, ULong, Byte, ULong> fieldsRow() {
        return (Row6) super.fieldsRow();
    }
}
