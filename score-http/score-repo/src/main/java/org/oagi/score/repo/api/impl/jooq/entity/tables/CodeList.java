/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.repo.api.impl.jooq.entity.tables;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function22;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row22;
import org.jooq.Schema;
import org.jooq.SelectField;
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
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CodeListRecord;


/**
 * This table stores information about a code list. When a code list is derived
 * from another code list, the whole set of code values belonging to the based
 * code list will be copied.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CodeList extends TableImpl<CodeListRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>oagi.code_list</code>
     */
    public static final CodeList CODE_LIST = new CodeList();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<CodeListRecord> getRecordType() {
        return CodeListRecord.class;
    }

    /**
     * The column <code>oagi.code_list.code_list_id</code>. Internal, primary
     * database key.
     */
    public final TableField<CodeListRecord, ULong> CODE_LIST_ID = createField(DSL.name("code_list_id"), SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "Internal, primary database key.");

    /**
     * The column <code>oagi.code_list.guid</code>. A globally unique identifier
     * (GUID).
     */
    public final TableField<CodeListRecord, String> GUID = createField(DSL.name("guid"), SQLDataType.CHAR(32).nullable(false), this, "A globally unique identifier (GUID).");

    /**
     * The column <code>oagi.code_list.enum_type_guid</code>. In the OAGIS Model
     * XML schema, a type, which keeps all the enumerated values, is  defined
     * separately from the type that represents a code list. This only applies
     * to some code lists. When that is the case, this column stores the GUID of
     * that enumeration type.
     */
    public final TableField<CodeListRecord, String> ENUM_TYPE_GUID = createField(DSL.name("enum_type_guid"), SQLDataType.VARCHAR(41).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "In the OAGIS Model XML schema, a type, which keeps all the enumerated values, is  defined separately from the type that represents a code list. This only applies to some code lists. When that is the case, this column stores the GUID of that enumeration type.");

    /**
     * The column <code>oagi.code_list.name</code>. Name of the code list.
     */
    public final TableField<CodeListRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(100).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "Name of the code list.");

    /**
     * The column <code>oagi.code_list.list_id</code>. External identifier.
     */
    public final TableField<CodeListRecord, String> LIST_ID = createField(DSL.name("list_id"), SQLDataType.VARCHAR(100).nullable(false), this, "External identifier.");

    /**
     * The column <code>oagi.code_list.version_id</code>. Code list version
     * number.
     */
    public final TableField<CodeListRecord, String> VERSION_ID = createField(DSL.name("version_id"), SQLDataType.VARCHAR(100).nullable(false), this, "Code list version number.");

    /**
     * The column <code>oagi.code_list.definition</code>. Description of the
     * code list.
     */
    public final TableField<CodeListRecord, String> DEFINITION = createField(DSL.name("definition"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "Description of the code list.");

    /**
     * The column <code>oagi.code_list.remark</code>. Usage information about
     * the code list.
     */
    public final TableField<CodeListRecord, String> REMARK = createField(DSL.name("remark"), SQLDataType.VARCHAR(225).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "Usage information about the code list.");

    /**
     * The column <code>oagi.code_list.definition_source</code>. This is
     * typically a URL which indicates the source of the code list's DEFINITION.
     */
    public final TableField<CodeListRecord, String> DEFINITION_SOURCE = createField(DSL.name("definition_source"), SQLDataType.VARCHAR(100).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "This is typically a URL which indicates the source of the code list's DEFINITION.");

    /**
     * The column <code>oagi.code_list.namespace_id</code>. Foreign key to the
     * NAMESPACE table. This is the namespace to which the entity belongs. This
     * namespace column is primarily used in the case the component is a user's
     * component because there is also a namespace assigned at the release
     * level.
     */
    public final TableField<CodeListRecord, ULong> NAMESPACE_ID = createField(DSL.name("namespace_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "Foreign key to the NAMESPACE table. This is the namespace to which the entity belongs. This namespace column is primarily used in the case the component is a user's component because there is also a namespace assigned at the release level.");

    /**
     * The column <code>oagi.code_list.based_code_list_id</code>. This is a
     * foreign key to the CODE_LIST table itself. This identifies the code list
     * on which this code list is based, if any. The derivation may be
     * restriction and/or extension.
     */
    public final TableField<CodeListRecord, ULong> BASED_CODE_LIST_ID = createField(DSL.name("based_code_list_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "This is a foreign key to the CODE_LIST table itself. This identifies the code list on which this code list is based, if any. The derivation may be restriction and/or extension.");

    /**
     * The column <code>oagi.code_list.extensible_indicator</code>. This is a
     * flag to indicate whether the code list is final and shall not be further
     * derived.
     */
    public final TableField<CodeListRecord, Byte> EXTENSIBLE_INDICATOR = createField(DSL.name("extensible_indicator"), SQLDataType.TINYINT.nullable(false), this, "This is a flag to indicate whether the code list is final and shall not be further derived.");

    /**
     * The column <code>oagi.code_list.is_deprecated</code>. Indicates whether
     * the code list is deprecated and should not be reused (i.e., no new
     * reference to this record should be allowed).
     */
    public final TableField<CodeListRecord, Byte> IS_DEPRECATED = createField(DSL.name("is_deprecated"), SQLDataType.TINYINT.defaultValue(DSL.field(DSL.raw("0"), SQLDataType.TINYINT)), this, "Indicates whether the code list is deprecated and should not be reused (i.e., no new reference to this record should be allowed).");

    /**
     * The column <code>oagi.code_list.replacement_code_list_id</code>. This
     * refers to a replacement if the record is deprecated.
     */
    public final TableField<CodeListRecord, ULong> REPLACEMENT_CODE_LIST_ID = createField(DSL.name("replacement_code_list_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "This refers to a replacement if the record is deprecated.");

    /**
     * The column <code>oagi.code_list.created_by</code>. Foreign key to the
     * APP_USER table. It indicates the user who created the code list.
     */
    public final TableField<CodeListRecord, ULong> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "Foreign key to the APP_USER table. It indicates the user who created the code list.");

    /**
     * The column <code>oagi.code_list.owner_user_id</code>. Foreign key to the
     * APP_USER table. This is the user who owns the entity, is allowed to edit
     * the entity, and who can transfer the ownership to another user.
     * 
     * The ownership can change throughout the history, but undoing shouldn't
     * rollback the ownership.
     */
    public final TableField<CodeListRecord, ULong> OWNER_USER_ID = createField(DSL.name("owner_user_id"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn't rollback the ownership.");

    /**
     * The column <code>oagi.code_list.last_updated_by</code>. Foreign key to
     * the APP_USER table. It identifies the user who last updated the code
     * list.
     */
    public final TableField<CodeListRecord, ULong> LAST_UPDATED_BY = createField(DSL.name("last_updated_by"), SQLDataType.BIGINTUNSIGNED.nullable(false), this, "Foreign key to the APP_USER table. It identifies the user who last updated the code list.");

    /**
     * The column <code>oagi.code_list.creation_timestamp</code>. Timestamp when
     * the code list was created.
     */
    public final TableField<CodeListRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "Timestamp when the code list was created.");

    /**
     * The column <code>oagi.code_list.last_update_timestamp</code>. Timestamp
     * when the code list was last updated.
     */
    public final TableField<CodeListRecord, LocalDateTime> LAST_UPDATE_TIMESTAMP = createField(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "Timestamp when the code list was last updated.");

    /**
     * The column <code>oagi.code_list.state</code>.
     */
    public final TableField<CodeListRecord, String> STATE = createField(DSL.name("state"), SQLDataType.VARCHAR(20).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>oagi.code_list.prev_code_list_id</code>. A self-foreign
     * key to indicate the previous history record.
     */
    public final TableField<CodeListRecord, ULong> PREV_CODE_LIST_ID = createField(DSL.name("prev_code_list_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "A self-foreign key to indicate the previous history record.");

    /**
     * The column <code>oagi.code_list.next_code_list_id</code>. A self-foreign
     * key to indicate the next history record.
     */
    public final TableField<CodeListRecord, ULong> NEXT_CODE_LIST_ID = createField(DSL.name("next_code_list_id"), SQLDataType.BIGINTUNSIGNED.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINTUNSIGNED)), this, "A self-foreign key to indicate the next history record.");

    private CodeList(Name alias, Table<CodeListRecord> aliased) {
        this(alias, aliased, null);
    }

    private CodeList(Name alias, Table<CodeListRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("This table stores information about a code list. When a code list is derived from another code list, the whole set of code values belonging to the based code list will be copied."), TableOptions.table());
    }

    /**
     * Create an aliased <code>oagi.code_list</code> table reference
     */
    public CodeList(String alias) {
        this(DSL.name(alias), CODE_LIST);
    }

    /**
     * Create an aliased <code>oagi.code_list</code> table reference
     */
    public CodeList(Name alias) {
        this(alias, CODE_LIST);
    }

    /**
     * Create a <code>oagi.code_list</code> table reference
     */
    public CodeList() {
        this(DSL.name("code_list"), null);
    }

    public <O extends Record> CodeList(Table<O> child, ForeignKey<O, CodeListRecord> key) {
        super(child, key, CODE_LIST);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Oagi.OAGI;
    }

    @Override
    public Identity<CodeListRecord, ULong> getIdentity() {
        return (Identity<CodeListRecord, ULong>) super.getIdentity();
    }

    @Override
    public UniqueKey<CodeListRecord> getPrimaryKey() {
        return Keys.KEY_CODE_LIST_PRIMARY;
    }

    @Override
    public List<ForeignKey<CodeListRecord, ?>> getReferences() {
        return Arrays.asList(Keys.CODE_LIST_NAMESPACE_ID_FK, Keys.CODE_LIST_BASED_CODE_LIST_ID_FK, Keys.CODE_LIST_REPLACEMENT_CODE_LIST_ID_FK, Keys.CODE_LIST_CREATED_BY_FK, Keys.CODE_LIST_OWNER_USER_ID_FK, Keys.CODE_LIST_LAST_UPDATED_BY_FK, Keys.CODE_LIST_PREV_CODE_LIST_ID_FK, Keys.CODE_LIST_NEXT_CODE_LIST_ID_FK);
    }

    private transient Namespace _namespace;
    private transient CodeList _codeListBasedCodeListIdFk;
    private transient CodeList _codeListReplacementCodeListIdFk;
    private transient AppUser _codeListCreatedByFk;
    private transient AppUser _codeListOwnerUserIdFk;
    private transient AppUser _codeListLastUpdatedByFk;
    private transient CodeList _codeListPrevCodeListIdFk;
    private transient CodeList _codeListNextCodeListIdFk;

    /**
     * Get the implicit join path to the <code>oagi.namespace</code> table.
     */
    public Namespace namespace() {
        if (_namespace == null)
            _namespace = new Namespace(this, Keys.CODE_LIST_NAMESPACE_ID_FK);

        return _namespace;
    }

    /**
     * Get the implicit join path to the <code>oagi.code_list</code> table, via
     * the <code>code_list_based_code_list_id_fk</code> key.
     */
    public CodeList codeListBasedCodeListIdFk() {
        if (_codeListBasedCodeListIdFk == null)
            _codeListBasedCodeListIdFk = new CodeList(this, Keys.CODE_LIST_BASED_CODE_LIST_ID_FK);

        return _codeListBasedCodeListIdFk;
    }

    /**
     * Get the implicit join path to the <code>oagi.code_list</code> table, via
     * the <code>code_list_replacement_code_list_id_fk</code> key.
     */
    public CodeList codeListReplacementCodeListIdFk() {
        if (_codeListReplacementCodeListIdFk == null)
            _codeListReplacementCodeListIdFk = new CodeList(this, Keys.CODE_LIST_REPLACEMENT_CODE_LIST_ID_FK);

        return _codeListReplacementCodeListIdFk;
    }

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>code_list_created_by_fk</code> key.
     */
    public AppUser codeListCreatedByFk() {
        if (_codeListCreatedByFk == null)
            _codeListCreatedByFk = new AppUser(this, Keys.CODE_LIST_CREATED_BY_FK);

        return _codeListCreatedByFk;
    }

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>code_list_owner_user_id_fk</code> key.
     */
    public AppUser codeListOwnerUserIdFk() {
        if (_codeListOwnerUserIdFk == null)
            _codeListOwnerUserIdFk = new AppUser(this, Keys.CODE_LIST_OWNER_USER_ID_FK);

        return _codeListOwnerUserIdFk;
    }

    /**
     * Get the implicit join path to the <code>oagi.app_user</code> table, via
     * the <code>code_list_last_updated_by_fk</code> key.
     */
    public AppUser codeListLastUpdatedByFk() {
        if (_codeListLastUpdatedByFk == null)
            _codeListLastUpdatedByFk = new AppUser(this, Keys.CODE_LIST_LAST_UPDATED_BY_FK);

        return _codeListLastUpdatedByFk;
    }

    /**
     * Get the implicit join path to the <code>oagi.code_list</code> table, via
     * the <code>code_list_prev_code_list_id_fk</code> key.
     */
    public CodeList codeListPrevCodeListIdFk() {
        if (_codeListPrevCodeListIdFk == null)
            _codeListPrevCodeListIdFk = new CodeList(this, Keys.CODE_LIST_PREV_CODE_LIST_ID_FK);

        return _codeListPrevCodeListIdFk;
    }

    /**
     * Get the implicit join path to the <code>oagi.code_list</code> table, via
     * the <code>code_list_next_code_list_id_fk</code> key.
     */
    public CodeList codeListNextCodeListIdFk() {
        if (_codeListNextCodeListIdFk == null)
            _codeListNextCodeListIdFk = new CodeList(this, Keys.CODE_LIST_NEXT_CODE_LIST_ID_FK);

        return _codeListNextCodeListIdFk;
    }

    @Override
    public CodeList as(String alias) {
        return new CodeList(DSL.name(alias), this);
    }

    @Override
    public CodeList as(Name alias) {
        return new CodeList(alias, this);
    }

    @Override
    public CodeList as(Table<?> alias) {
        return new CodeList(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public CodeList rename(String name) {
        return new CodeList(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public CodeList rename(Name name) {
        return new CodeList(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public CodeList rename(Table<?> name) {
        return new CodeList(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row22 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row22<ULong, String, String, String, String, String, String, String, String, ULong, ULong, Byte, Byte, ULong, ULong, ULong, ULong, LocalDateTime, LocalDateTime, String, ULong, ULong> fieldsRow() {
        return (Row22) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function22<? super ULong, ? super String, ? super String, ? super String, ? super String, ? super String, ? super String, ? super String, ? super String, ? super ULong, ? super ULong, ? super Byte, ? super Byte, ? super ULong, ? super ULong, ? super ULong, ? super ULong, ? super LocalDateTime, ? super LocalDateTime, ? super String, ? super ULong, ? super ULong, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function22<? super ULong, ? super String, ? super String, ? super String, ? super String, ? super String, ? super String, ? super String, ? super String, ? super ULong, ? super ULong, ? super Byte, ? super Byte, ? super ULong, ? super ULong, ? super ULong, ? super ULong, ? super LocalDateTime, ? super LocalDateTime, ? super String, ? super ULong, ? super ULong, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
