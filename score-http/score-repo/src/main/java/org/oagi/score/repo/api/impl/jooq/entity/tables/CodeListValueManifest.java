/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.repo.api.impl.jooq.entity.tables;


import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row8;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.Keys;
import org.oagi.score.repo.api.impl.jooq.entity.Oagi;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CodeListValueManifestRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CodeListValueManifest extends TableImpl<CodeListValueManifestRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.code_list_value_manifest</code>
     */
    public static final CodeListValueManifest CODE_LIST_VALUE_MANIFEST = new CodeListValueManifest();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<CodeListValueManifestRecord> getRecordType() {
        return CodeListValueManifestRecord.class;
    }

    /**
     * The column <code>oagi.code_list_value_manifest.code_list_value_manifest_id</code>.
     */
    public final TableField<CodeListValueManifestRecord, ULong> CODE_LIST_VALUE_MANIFEST_ID = createField(DSL.name("code_list_value_manifest_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "");

    /**
     * The column <code>oagi.code_list_value_manifest.release_id</code>.
     */
    public final TableField<CodeListValueManifestRecord, ULong> RELEASE_ID = createField(DSL.name("release_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.code_list_value_manifest.code_list_value_id</code>.
     */
    public final TableField<CodeListValueManifestRecord, ULong> CODE_LIST_VALUE_ID = createField(DSL.name("code_list_value_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.code_list_value_manifest.code_list_manifest_id</code>.
     */
    public final TableField<CodeListValueManifestRecord, ULong> CODE_LIST_MANIFEST_ID = createField(DSL.name("code_list_manifest_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>oagi.code_list_value_manifest.conflict</code>. This indicates that there is a conflict between self and relationship.
     */
    public final TableField<CodeListValueManifestRecord, Byte> CONFLICT = createField(DSL.name("conflict"), SQLDataType.TINYINT.nullable(false).defaultValue(DSL.inline("0", SQLDataType.TINYINT)), this, "This indicates that there is a conflict between self and relationship.");

    /**
     * The column <code>oagi.code_list_value_manifest.replacement_code_list_value_manifest_id</code>. This refers to a replacement manifest if the record is deprecated.
     */
    public final TableField<CodeListValueManifestRecord, ULong> REPLACEMENT_CODE_LIST_VALUE_MANIFEST_ID = createField(DSL.name("replacement_code_list_value_manifest_id"), SQLDataType.BIGINTUNSIGNED, this, "This refers to a replacement manifest if the record is deprecated.");

    /**
     * The column <code>oagi.code_list_value_manifest.prev_code_list_value_manifest_id</code>.
     */
    public final TableField<CodeListValueManifestRecord, ULong> PREV_CODE_LIST_VALUE_MANIFEST_ID = createField(DSL.name("prev_code_list_value_manifest_id"), SQLDataType.BIGINTUNSIGNED, this, "");

    /**
     * The column <code>oagi.code_list_value_manifest.next_code_list_value_manifest_id</code>.
     */
    public final TableField<CodeListValueManifestRecord, ULong> NEXT_CODE_LIST_VALUE_MANIFEST_ID = createField(DSL.name("next_code_list_value_manifest_id"), SQLDataType.BIGINTUNSIGNED, this, "");

    private CodeListValueManifest(Name alias, Table<CodeListValueManifestRecord> aliased) {
        this(alias, aliased, null);
    }

    private CodeListValueManifest(Name alias, Table<CodeListValueManifestRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>oagi.code_list_value_manifest</code> table reference
     */
    public CodeListValueManifest(String alias) {
        this(DSL.name(alias), CODE_LIST_VALUE_MANIFEST);
    }

    /**
     * Create an aliased <code>oagi.code_list_value_manifest</code> table reference
     */
    public CodeListValueManifest(Name alias) {
        this(alias, CODE_LIST_VALUE_MANIFEST);
    }

    /**
     * Create a <code>oagi.code_list_value_manifest</code> table reference
     */
    public CodeListValueManifest() {
        this(DSL.name("code_list_value_manifest"), null);
    }

    public <O extends Record> CodeListValueManifest(Table<O> child, ForeignKey<O, CodeListValueManifestRecord> key) {
        super(child, key, CODE_LIST_VALUE_MANIFEST);
    }

    @Override
    public Schema getSchema() {
        return Oagi.OAGI;
    }

    @Override
    public Identity<CodeListValueManifestRecord, ULong> getIdentity() {
        return (Identity<CodeListValueManifestRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<CodeListValueManifestRecord> getPrimaryKey() {
        return Keys.KEY_CODE_LIST_VALUE_MANIFEST_PRIMARY;
    }

    @Override
    public List<UniqueKey<CodeListValueManifestRecord>> getKeys() {
        return Arrays.<UniqueKey<CodeListValueManifestRecord>>asList(Keys.KEY_CODE_LIST_VALUE_MANIFEST_PRIMARY);
    }

    @Override
    public List<ForeignKey<CodeListValueManifestRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<CodeListValueManifestRecord, ?>>asList(Keys.CODE_LIST_VALUE_MANIFEST_RELEASE_ID_FK, Keys.CODE_LIST_VALUE_MANIFEST_CODE_LIST_VALUE_ID_FK, Keys.CODE_LIST_VALUE_MANIFEST_CODE_LIST_MANIFEST_ID_FK, Keys.CODE_LIST_VALUE_REPLACEMENT_CODE_LIST_VALUE_MANIFEST_ID_FK, Keys.CODE_LIST_VALUE_MANIFEST_PREV_CODE_LIST_VALUE_MANIFEST_ID_FK, Keys.CODE_LIST_VALUE_MANIFEST_NEXT_CODE_LIST_VALUE_MANIFEST_ID_FK);
    }

    private transient Release _release;
    private transient CodeListValue _codeListValue;
    private transient CodeListManifest _codeListManifest;
    private transient CodeListValueManifest _codeListValueReplacementCodeListValueManifestIdFk;
    private transient CodeListValueManifest _codeListValueManifestPrevCodeListValueManifestIdFk;
    private transient CodeListValueManifest _codeListValueManifestNextCodeListValueManifestIdFk;

    public Release release() {
        if (_release == null)
            _release = new Release(this, Keys.CODE_LIST_VALUE_MANIFEST_RELEASE_ID_FK);

        return _release;
    }

    public CodeListValue codeListValue() {
        if (_codeListValue == null)
            _codeListValue = new CodeListValue(this, Keys.CODE_LIST_VALUE_MANIFEST_CODE_LIST_VALUE_ID_FK);

        return _codeListValue;
    }

    public CodeListManifest codeListManifest() {
        if (_codeListManifest == null)
            _codeListManifest = new CodeListManifest(this, Keys.CODE_LIST_VALUE_MANIFEST_CODE_LIST_MANIFEST_ID_FK);

        return _codeListManifest;
    }

    public CodeListValueManifest codeListValueReplacementCodeListValueManifestIdFk() {
        if (_codeListValueReplacementCodeListValueManifestIdFk == null)
            _codeListValueReplacementCodeListValueManifestIdFk = new CodeListValueManifest(this, Keys.CODE_LIST_VALUE_REPLACEMENT_CODE_LIST_VALUE_MANIFEST_ID_FK);

        return _codeListValueReplacementCodeListValueManifestIdFk;
    }

    public CodeListValueManifest codeListValueManifestPrevCodeListValueManifestIdFk() {
        if (_codeListValueManifestPrevCodeListValueManifestIdFk == null)
            _codeListValueManifestPrevCodeListValueManifestIdFk = new CodeListValueManifest(this, Keys.CODE_LIST_VALUE_MANIFEST_PREV_CODE_LIST_VALUE_MANIFEST_ID_FK);

        return _codeListValueManifestPrevCodeListValueManifestIdFk;
    }

    public CodeListValueManifest codeListValueManifestNextCodeListValueManifestIdFk() {
        if (_codeListValueManifestNextCodeListValueManifestIdFk == null)
            _codeListValueManifestNextCodeListValueManifestIdFk = new CodeListValueManifest(this, Keys.CODE_LIST_VALUE_MANIFEST_NEXT_CODE_LIST_VALUE_MANIFEST_ID_FK);

        return _codeListValueManifestNextCodeListValueManifestIdFk;
    }

    @Override
    public CodeListValueManifest as(String alias) {
        return new CodeListValueManifest(DSL.name(alias), this);
    }

    @Override
    public CodeListValueManifest as(Name alias) {
        return new CodeListValueManifest(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public CodeListValueManifest rename(String name) {
        return new CodeListValueManifest(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public CodeListValueManifest rename(Name name) {
        return new CodeListValueManifest(name, null);
    }

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row8<ULong, ULong, ULong, ULong, Byte, ULong, ULong, ULong> fieldsRow() {
        return (Row8) super.fieldsRow();
    }
}
