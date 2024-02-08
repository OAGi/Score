/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.repo.api.impl.jooq.entity.tables.records;


import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.OasParameterLink;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OasParameterLinkRecord extends UpdatableRecordImpl<OasParameterLinkRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.oas_parameter_link.oas_parameter_link_id</code>.
     * The primary key of the record.
     */
    public void setOasParameterLinkId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.oas_parameter_link.oas_parameter_link_id</code>.
     * The primary key of the record.
     */
    public ULong getOasParameterLinkId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.oas_parameter_link.oas_response_id</code>.
     */
    public void setOasResponseId(ULong value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.oas_parameter_link.oas_response_id</code>.
     */
    public ULong getOasResponseId() {
        return (ULong) get(1);
    }

    /**
     * Setter for <code>oagi.oas_parameter_link.oas_parameter_id</code>.
     */
    public void setOasParameterId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.oas_parameter_link.oas_parameter_id</code>.
     */
    public ULong getOasParameterId() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.oas_parameter_link.oas_operation_id</code>.
     */
    public void setOasOperationId(ULong value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.oas_parameter_link.oas_operation_id</code>.
     */
    public ULong getOasOperationId() {
        return (ULong) get(3);
    }

    /**
     * Setter for <code>oagi.oas_parameter_link.expression</code>. jsonPathSnip
     * for example '$response.body#/purchaseOrderHeader.identifier'
     */
    public void setExpression(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.oas_parameter_link.expression</code>. jsonPathSnip
     * for example '$response.body#/purchaseOrderHeader.identifier'
     */
    public String getExpression() {
        return (String) get(4);
    }

    /**
     * Setter for <code>oagi.oas_parameter_link.description</code>.
     */
    public void setDescription(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.oas_parameter_link.description</code>.
     */
    public String getDescription() {
        return (String) get(5);
    }

    /**
     * Setter for <code>oagi.oas_parameter_link.created_by</code>. The user who
     * creates the record.
     */
    public void setCreatedBy(ULong value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.oas_parameter_link.created_by</code>. The user who
     * creates the record.
     */
    public ULong getCreatedBy() {
        return (ULong) get(6);
    }

    /**
     * Setter for <code>oagi.oas_parameter_link.last_updated_by</code>. The user
     * who last updates the record.
     */
    public void setLastUpdatedBy(ULong value) {
        set(7, value);
    }

    /**
     * Getter for <code>oagi.oas_parameter_link.last_updated_by</code>. The user
     * who last updates the record.
     */
    public ULong getLastUpdatedBy() {
        return (ULong) get(7);
    }

    /**
     * Setter for <code>oagi.oas_parameter_link.creation_timestamp</code>. The
     * timestamp when the record is created.
     */
    public void setCreationTimestamp(LocalDateTime value) {
        set(8, value);
    }

    /**
     * Getter for <code>oagi.oas_parameter_link.creation_timestamp</code>. The
     * timestamp when the record is created.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(8);
    }

    /**
     * Setter for <code>oagi.oas_parameter_link.last_update_timestamp</code>.
     * The timestamp when the record is last updated.
     */
    public void setLastUpdateTimestamp(LocalDateTime value) {
        set(9, value);
    }

    /**
     * Getter for <code>oagi.oas_parameter_link.last_update_timestamp</code>.
     * The timestamp when the record is last updated.
     */
    public LocalDateTime getLastUpdateTimestamp() {
        return (LocalDateTime) get(9);
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
     * Create a detached OasParameterLinkRecord
     */
    public OasParameterLinkRecord() {
        super(OasParameterLink.OAS_PARAMETER_LINK);
    }

    /**
     * Create a detached, initialised OasParameterLinkRecord
     */
    public OasParameterLinkRecord(ULong oasParameterLinkId, ULong oasResponseId, ULong oasParameterId, ULong oasOperationId, String expression, String description, ULong createdBy, ULong lastUpdatedBy, LocalDateTime creationTimestamp, LocalDateTime lastUpdateTimestamp) {
        super(OasParameterLink.OAS_PARAMETER_LINK);

        setOasParameterLinkId(oasParameterLinkId);
        setOasResponseId(oasResponseId);
        setOasParameterId(oasParameterId);
        setOasOperationId(oasOperationId);
        setExpression(expression);
        setDescription(description);
        setCreatedBy(createdBy);
        setLastUpdatedBy(lastUpdatedBy);
        setCreationTimestamp(creationTimestamp);
        setLastUpdateTimestamp(lastUpdateTimestamp);
        resetChangedOnNotNull();
    }
}
