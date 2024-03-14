/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Xbt;


/**
 * This table stores XML schema built-in types and OAGIS built-in types. OAGIS
 * built-in types are those types defined in the XMLSchemaBuiltinType and the
 * XMLSchemaBuiltinType Patterns schemas.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class XbtRecord extends UpdatableRecordImpl<XbtRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.xbt.xbt_id</code>. Primary, internal database key.
     */
    public void setXbtId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.xbt.xbt_id</code>. Primary, internal database key.
     */
    public ULong getXbtId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.xbt.guid</code>. A globally unique identifier
     * (GUID).
     */
    public void setGuid(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.xbt.guid</code>. A globally unique identifier
     * (GUID).
     */
    public String getGuid() {
        return (String) get(1);
    }

    /**
     * Setter for <code>oagi.xbt.name</code>. Human understandable name of the
     * built-in type.
     */
    public void setName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.xbt.name</code>. Human understandable name of the
     * built-in type.
     */
    public String getName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>oagi.xbt.builtIn_type</code>. Built-in type as it should
     * appear in the XML schema including the namespace prefix. Namespace prefix
     * for the XML schema namespace is assumed to be 'xsd' and a default prefix
     * for the OAGIS built-int type.
     */
    public void setBuiltinType(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.xbt.builtIn_type</code>. Built-in type as it should
     * appear in the XML schema including the namespace prefix. Namespace prefix
     * for the XML schema namespace is assumed to be 'xsd' and a default prefix
     * for the OAGIS built-int type.
     */
    public String getBuiltinType() {
        return (String) get(3);
    }

    /**
     * Setter for <code>oagi.xbt.jbt_draft05_map</code>.
     */
    public void setJbtDraft05Map(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.xbt.jbt_draft05_map</code>.
     */
    public String getJbtDraft05Map() {
        return (String) get(4);
    }

    /**
     * Setter for <code>oagi.xbt.openapi30_map</code>.
     */
    public void setOpenapi30Map(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.xbt.openapi30_map</code>.
     */
    public String getOpenapi30Map() {
        return (String) get(5);
    }

    /**
     * Setter for <code>oagi.xbt.avro_map</code>.
     */
    public void setAvroMap(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.xbt.avro_map</code>.
     */
    public String getAvroMap() {
        return (String) get(6);
    }

    /**
     * Setter for <code>oagi.xbt.subtype_of_xbt_id</code>. Foreign key to the
     * XBT table itself. It indicates a super type of this XSD built-in type.
     */
    public void setSubtypeOfXbtId(ULong value) {
        set(7, value);
    }

    /**
     * Getter for <code>oagi.xbt.subtype_of_xbt_id</code>. Foreign key to the
     * XBT table itself. It indicates a super type of this XSD built-in type.
     */
    public ULong getSubtypeOfXbtId() {
        return (ULong) get(7);
    }

    /**
     * Setter for <code>oagi.xbt.schema_definition</code>.
     */
    public void setSchemaDefinition(String value) {
        set(8, value);
    }

    /**
     * Getter for <code>oagi.xbt.schema_definition</code>.
     */
    public String getSchemaDefinition() {
        return (String) get(8);
    }

    /**
     * Setter for <code>oagi.xbt.revision_doc</code>.
     */
    public void setRevisionDoc(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>oagi.xbt.revision_doc</code>.
     */
    public String getRevisionDoc() {
        return (String) get(9);
    }

    /**
     * Setter for <code>oagi.xbt.state</code>.
     */
    public void setState(Integer value) {
        set(10, value);
    }

    /**
     * Getter for <code>oagi.xbt.state</code>.
     */
    public Integer getState() {
        return (Integer) get(10);
    }

    /**
     * Setter for <code>oagi.xbt.created_by</code>.
     */
    public void setCreatedBy(ULong value) {
        set(11, value);
    }

    /**
     * Getter for <code>oagi.xbt.created_by</code>.
     */
    public ULong getCreatedBy() {
        return (ULong) get(11);
    }

    /**
     * Setter for <code>oagi.xbt.owner_user_id</code>.
     */
    public void setOwnerUserId(ULong value) {
        set(12, value);
    }

    /**
     * Getter for <code>oagi.xbt.owner_user_id</code>.
     */
    public ULong getOwnerUserId() {
        return (ULong) get(12);
    }

    /**
     * Setter for <code>oagi.xbt.last_updated_by</code>.
     */
    public void setLastUpdatedBy(ULong value) {
        set(13, value);
    }

    /**
     * Getter for <code>oagi.xbt.last_updated_by</code>.
     */
    public ULong getLastUpdatedBy() {
        return (ULong) get(13);
    }

    /**
     * Setter for <code>oagi.xbt.creation_timestamp</code>.
     */
    public void setCreationTimestamp(LocalDateTime value) {
        set(14, value);
    }

    /**
     * Getter for <code>oagi.xbt.creation_timestamp</code>.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(14);
    }

    /**
     * Setter for <code>oagi.xbt.last_update_timestamp</code>.
     */
    public void setLastUpdateTimestamp(LocalDateTime value) {
        set(15, value);
    }

    /**
     * Getter for <code>oagi.xbt.last_update_timestamp</code>.
     */
    public LocalDateTime getLastUpdateTimestamp() {
        return (LocalDateTime) get(15);
    }

    /**
     * Setter for <code>oagi.xbt.is_deprecated</code>.
     */
    public void setIsDeprecated(Byte value) {
        set(16, value);
    }

    /**
     * Getter for <code>oagi.xbt.is_deprecated</code>.
     */
    public Byte getIsDeprecated() {
        return (Byte) get(16);
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
     * Create a detached XbtRecord
     */
    public XbtRecord() {
        super(Xbt.XBT);
    }

    /**
     * Create a detached, initialised XbtRecord
     */
    public XbtRecord(ULong xbtId, String guid, String name, String builtinType, String jbtDraft05Map, String openapi30Map, String avroMap, ULong subtypeOfXbtId, String schemaDefinition, String revisionDoc, Integer state, ULong createdBy, ULong ownerUserId, ULong lastUpdatedBy, LocalDateTime creationTimestamp, LocalDateTime lastUpdateTimestamp, Byte isDeprecated) {
        super(Xbt.XBT);

        setXbtId(xbtId);
        setGuid(guid);
        setName(name);
        setBuiltinType(builtinType);
        setJbtDraft05Map(jbtDraft05Map);
        setOpenapi30Map(openapi30Map);
        setAvroMap(avroMap);
        setSubtypeOfXbtId(subtypeOfXbtId);
        setSchemaDefinition(schemaDefinition);
        setRevisionDoc(revisionDoc);
        setState(state);
        setCreatedBy(createdBy);
        setOwnerUserId(ownerUserId);
        setLastUpdatedBy(lastUpdatedBy);
        setCreationTimestamp(creationTimestamp);
        setLastUpdateTimestamp(lastUpdateTimestamp);
        setIsDeprecated(isDeprecated);
        resetChangedOnNotNull();
    }
}
