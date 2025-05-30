/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.CodeListValue;


/**
 * Each record in this table stores a code list value of a code list. A code
 * list value may be inherited from another code list on which it is based.
 * However, inherited value may be restricted (i.e., disabled and cannot be
 * used) in this code list, i.e., the USED_INDICATOR = false. If the value
 * cannot be used since the based code list, then the LOCKED_INDICATOR = TRUE,
 * because the USED_INDICATOR of such code list value is FALSE by default and
 * can no longer be changed.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class CodeListValueRecord extends UpdatableRecordImpl<CodeListValueRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.code_list_value.code_list_value_id</code>.
     * Internal, primary database key.
     */
    public void setCodeListValueId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.code_list_value_id</code>.
     * Internal, primary database key.
     */
    public ULong getCodeListValueId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.code_list_value.guid</code>. A globally unique
     * identifier (GUID).
     */
    public void setGuid(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.guid</code>. A globally unique
     * identifier (GUID).
     */
    public String getGuid() {
        return (String) get(1);
    }

    /**
     * Setter for <code>oagi.code_list_value.code_list_id</code>. Foreign key to
     * the CODE_LIST table. It indicates the code list this code value belonging
     * to.
     */
    public void setCodeListId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.code_list_id</code>. Foreign key to
     * the CODE_LIST table. It indicates the code list this code value belonging
     * to.
     */
    public ULong getCodeListId() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.code_list_value.based_code_list_value_id</code>.
     * Foreign key to the CODE_LIST_VALUE table itself. This column is used when
     * the CODE_LIST is derived from the based CODE_LIST.
     */
    public void setBasedCodeListValueId(ULong value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.based_code_list_value_id</code>.
     * Foreign key to the CODE_LIST_VALUE table itself. This column is used when
     * the CODE_LIST is derived from the based CODE_LIST.
     */
    public ULong getBasedCodeListValueId() {
        return (ULong) get(3);
    }

    /**
     * Setter for <code>oagi.code_list_value.value</code>. The code list value
     * used in the instance data, e.g., EA, US-EN.
     */
    public void setValue(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.value</code>. The code list value
     * used in the instance data, e.g., EA, US-EN.
     */
    public String getValue() {
        return (String) get(4);
    }

    /**
     * Setter for <code>oagi.code_list_value.meaning</code>. The description or
     * explanation of the code list value, e.g., 'Each' for EA, 'English' for
     * EN.
     */
    public void setMeaning(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.meaning</code>. The description or
     * explanation of the code list value, e.g., 'Each' for EA, 'English' for
     * EN.
     */
    public String getMeaning() {
        return (String) get(5);
    }

    /**
     * Setter for <code>oagi.code_list_value.definition</code>. Long description
     * or explannation of the code list value, e.g., 'EA is a discrete quantity
     * for counting each unit of an item, such as, 2 shampoo bottles, 3 box of
     * cereals'.
     */
    public void setDefinition(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.definition</code>. Long description
     * or explannation of the code list value, e.g., 'EA is a discrete quantity
     * for counting each unit of an item, such as, 2 shampoo bottles, 3 box of
     * cereals'.
     */
    public String getDefinition() {
        return (String) get(6);
    }

    /**
     * Setter for <code>oagi.code_list_value.definition_source</code>. This is
     * typically a URL identifying the source of the DEFINITION column.
     */
    public void setDefinitionSource(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.definition_source</code>. This is
     * typically a URL identifying the source of the DEFINITION column.
     */
    public String getDefinitionSource() {
        return (String) get(7);
    }

    /**
     * Setter for <code>oagi.code_list_value.is_deprecated</code>. Indicates
     * whether the code list value is deprecated and should not be reused (i.e.,
     * no new reference to this record should be allowed).
     */
    public void setIsDeprecated(Byte value) {
        set(8, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.is_deprecated</code>. Indicates
     * whether the code list value is deprecated and should not be reused (i.e.,
     * no new reference to this record should be allowed).
     */
    public Byte getIsDeprecated() {
        return (Byte) get(8);
    }

    /**
     * Setter for
     * <code>oagi.code_list_value.replacement_code_list_value_id</code>. This
     * refers to a replacement if the record is deprecated.
     */
    public void setReplacementCodeListValueId(ULong value) {
        set(9, value);
    }

    /**
     * Getter for
     * <code>oagi.code_list_value.replacement_code_list_value_id</code>. This
     * refers to a replacement if the record is deprecated.
     */
    public ULong getReplacementCodeListValueId() {
        return (ULong) get(9);
    }

    /**
     * Setter for <code>oagi.code_list_value.created_by</code>. Foreign key to
     * the APP_USER table. It indicates the user who created the code list.
     */
    public void setCreatedBy(ULong value) {
        set(10, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.created_by</code>. Foreign key to
     * the APP_USER table. It indicates the user who created the code list.
     */
    public ULong getCreatedBy() {
        return (ULong) get(10);
    }

    /**
     * Setter for <code>oagi.code_list_value.owner_user_id</code>. Foreign key
     * to the APP_USER table. This is the user who owns the entity, is allowed
     * to edit the entity, and who can transfer the ownership to another user.
     * 
     * The ownership can change throughout the history, but undoing shouldn't
     * rollback the ownership.
     */
    public void setOwnerUserId(ULong value) {
        set(11, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.owner_user_id</code>. Foreign key
     * to the APP_USER table. This is the user who owns the entity, is allowed
     * to edit the entity, and who can transfer the ownership to another user.
     * 
     * The ownership can change throughout the history, but undoing shouldn't
     * rollback the ownership.
     */
    public ULong getOwnerUserId() {
        return (ULong) get(11);
    }

    /**
     * Setter for <code>oagi.code_list_value.last_updated_by</code>. Foreign key
     * to the APP_USER table. It identifies the user who last updated the code
     * list.
     */
    public void setLastUpdatedBy(ULong value) {
        set(12, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.last_updated_by</code>. Foreign key
     * to the APP_USER table. It identifies the user who last updated the code
     * list.
     */
    public ULong getLastUpdatedBy() {
        return (ULong) get(12);
    }

    /**
     * Setter for <code>oagi.code_list_value.creation_timestamp</code>.
     * Timestamp when the code list was created.
     */
    public void setCreationTimestamp(LocalDateTime value) {
        set(13, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.creation_timestamp</code>.
     * Timestamp when the code list was created.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(13);
    }

    /**
     * Setter for <code>oagi.code_list_value.last_update_timestamp</code>.
     * Timestamp when the code list was last updated.
     */
    public void setLastUpdateTimestamp(LocalDateTime value) {
        set(14, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.last_update_timestamp</code>.
     * Timestamp when the code list was last updated.
     */
    public LocalDateTime getLastUpdateTimestamp() {
        return (LocalDateTime) get(14);
    }

    /**
     * Setter for <code>oagi.code_list_value.prev_code_list_value_id</code>. A
     * self-foreign key to indicate the previous history record.
     */
    public void setPrevCodeListValueId(ULong value) {
        set(15, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.prev_code_list_value_id</code>. A
     * self-foreign key to indicate the previous history record.
     */
    public ULong getPrevCodeListValueId() {
        return (ULong) get(15);
    }

    /**
     * Setter for <code>oagi.code_list_value.next_code_list_value_id</code>. A
     * self-foreign key to indicate the next history record.
     */
    public void setNextCodeListValueId(ULong value) {
        set(16, value);
    }

    /**
     * Getter for <code>oagi.code_list_value.next_code_list_value_id</code>. A
     * self-foreign key to indicate the next history record.
     */
    public ULong getNextCodeListValueId() {
        return (ULong) get(16);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<ULong> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached CodeListValueRecord
     */
    public CodeListValueRecord() {
        super(CodeListValue.CODE_LIST_VALUE);
    }

    /**
     * Create a detached, initialised CodeListValueRecord
     */
    public CodeListValueRecord(ULong codeListValueId, String guid, ULong codeListId, ULong basedCodeListValueId, String value, String meaning, String definition, String definitionSource, Byte isDeprecated, ULong replacementCodeListValueId, ULong createdBy, ULong ownerUserId, ULong lastUpdatedBy, LocalDateTime creationTimestamp, LocalDateTime lastUpdateTimestamp, ULong prevCodeListValueId, ULong nextCodeListValueId) {
        super(CodeListValue.CODE_LIST_VALUE);

        setCodeListValueId(codeListValueId);
        setGuid(guid);
        setCodeListId(codeListId);
        setBasedCodeListValueId(basedCodeListValueId);
        setValue(value);
        setMeaning(meaning);
        setDefinition(definition);
        setDefinitionSource(definitionSource);
        setIsDeprecated(isDeprecated);
        setReplacementCodeListValueId(replacementCodeListValueId);
        setCreatedBy(createdBy);
        setOwnerUserId(ownerUserId);
        setLastUpdatedBy(lastUpdatedBy);
        setCreationTimestamp(creationTimestamp);
        setLastUpdateTimestamp(lastUpdateTimestamp);
        setPrevCodeListValueId(prevCodeListValueId);
        setNextCodeListValueId(nextCodeListValueId);
        resetTouchedOnNotNull();
    }
}
