/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records;


import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.OasResponse;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class OasResponseRecord extends UpdatableRecordImpl<OasResponseRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.oas_response.oas_response_id</code>. The primary
     * key of the record.
     */
    public void setOasResponseId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.oas_response.oas_response_id</code>. The primary
     * key of the record.
     */
    public ULong getOasResponseId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.oas_response.oas_operation_id</code>.
     */
    public void setOasOperationId(ULong value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.oas_response.oas_operation_id</code>.
     */
    public ULong getOasOperationId() {
        return (ULong) get(1);
    }

    /**
     * Setter for <code>oagi.oas_response.http_status_code</code>.
     */
    public void setHttpStatusCode(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.oas_response.http_status_code</code>.
     */
    public Integer getHttpStatusCode() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>oagi.oas_response.description</code>. A brief
     * description of the response body. This could contain examples of use.
     * CommonMark syntax MAY be used for rich text representation.
     */
    public void setDescription(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.oas_response.description</code>. A brief
     * description of the response body. This could contain examples of use.
     * CommonMark syntax MAY be used for rich text representation.
     */
    public String getDescription() {
        return (String) get(3);
    }

    /**
     * Setter for <code>oagi.oas_response.oas_message_body_id</code>.
     */
    public void setOasMessageBodyId(ULong value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.oas_response.oas_message_body_id</code>.
     */
    public ULong getOasMessageBodyId() {
        return (ULong) get(4);
    }

    /**
     * Setter for <code>oagi.oas_response.make_array_indicator</code>.
     */
    public void setMakeArrayIndicator(Byte value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.oas_response.make_array_indicator</code>.
     */
    public Byte getMakeArrayIndicator() {
        return (Byte) get(5);
    }

    /**
     * Setter for <code>oagi.oas_response.suppress_root_indicator</code>.
     */
    public void setSuppressRootIndicator(Byte value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.oas_response.suppress_root_indicator</code>.
     */
    public Byte getSuppressRootIndicator() {
        return (Byte) get(6);
    }

    /**
     * Setter for
     * <code>oagi.oas_response.meta_header_top_level_asbiep_id</code>.
     */
    public void setMetaHeaderTopLevelAsbiepId(ULong value) {
        set(7, value);
    }

    /**
     * Getter for
     * <code>oagi.oas_response.meta_header_top_level_asbiep_id</code>.
     */
    public ULong getMetaHeaderTopLevelAsbiepId() {
        return (ULong) get(7);
    }

    /**
     * Setter for <code>oagi.oas_response.pagination_top_level_asbiep_id</code>.
     */
    public void setPaginationTopLevelAsbiepId(ULong value) {
        set(8, value);
    }

    /**
     * Getter for <code>oagi.oas_response.pagination_top_level_asbiep_id</code>.
     */
    public ULong getPaginationTopLevelAsbiepId() {
        return (ULong) get(8);
    }

    /**
     * Setter for <code>oagi.oas_response.include_confirm_indicator</code>.
     */
    public void setIncludeConfirmIndicator(Byte value) {
        set(9, value);
    }

    /**
     * Getter for <code>oagi.oas_response.include_confirm_indicator</code>.
     */
    public Byte getIncludeConfirmIndicator() {
        return (Byte) get(9);
    }

    /**
     * Setter for <code>oagi.oas_response.created_by</code>. The user who
     * creates the record.
     */
    public void setCreatedBy(ULong value) {
        set(10, value);
    }

    /**
     * Getter for <code>oagi.oas_response.created_by</code>. The user who
     * creates the record.
     */
    public ULong getCreatedBy() {
        return (ULong) get(10);
    }

    /**
     * Setter for <code>oagi.oas_response.last_updated_by</code>. The user who
     * last updates the record.
     */
    public void setLastUpdatedBy(ULong value) {
        set(11, value);
    }

    /**
     * Getter for <code>oagi.oas_response.last_updated_by</code>. The user who
     * last updates the record.
     */
    public ULong getLastUpdatedBy() {
        return (ULong) get(11);
    }

    /**
     * Setter for <code>oagi.oas_response.creation_timestamp</code>. The
     * timestamp when the record is created.
     */
    public void setCreationTimestamp(LocalDateTime value) {
        set(12, value);
    }

    /**
     * Getter for <code>oagi.oas_response.creation_timestamp</code>. The
     * timestamp when the record is created.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(12);
    }

    /**
     * Setter for <code>oagi.oas_response.last_update_timestamp</code>. The
     * timestamp when the record is last updated.
     */
    public void setLastUpdateTimestamp(LocalDateTime value) {
        set(13, value);
    }

    /**
     * Getter for <code>oagi.oas_response.last_update_timestamp</code>. The
     * timestamp when the record is last updated.
     */
    public LocalDateTime getLastUpdateTimestamp() {
        return (LocalDateTime) get(13);
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
     * Create a detached OasResponseRecord
     */
    public OasResponseRecord() {
        super(OasResponse.OAS_RESPONSE);
    }

    /**
     * Create a detached, initialised OasResponseRecord
     */
    public OasResponseRecord(ULong oasResponseId, ULong oasOperationId, Integer httpStatusCode, String description, ULong oasMessageBodyId, Byte makeArrayIndicator, Byte suppressRootIndicator, ULong metaHeaderTopLevelAsbiepId, ULong paginationTopLevelAsbiepId, Byte includeConfirmIndicator, ULong createdBy, ULong lastUpdatedBy, LocalDateTime creationTimestamp, LocalDateTime lastUpdateTimestamp) {
        super(OasResponse.OAS_RESPONSE);

        setOasResponseId(oasResponseId);
        setOasOperationId(oasOperationId);
        setHttpStatusCode(httpStatusCode);
        setDescription(description);
        setOasMessageBodyId(oasMessageBodyId);
        setMakeArrayIndicator(makeArrayIndicator);
        setSuppressRootIndicator(suppressRootIndicator);
        setMetaHeaderTopLevelAsbiepId(metaHeaderTopLevelAsbiepId);
        setPaginationTopLevelAsbiepId(paginationTopLevelAsbiepId);
        setIncludeConfirmIndicator(includeConfirmIndicator);
        setCreatedBy(createdBy);
        setLastUpdatedBy(lastUpdatedBy);
        setCreationTimestamp(creationTimestamp);
        setLastUpdateTimestamp(lastUpdateTimestamp);
        resetTouchedOnNotNull();
    }
}
