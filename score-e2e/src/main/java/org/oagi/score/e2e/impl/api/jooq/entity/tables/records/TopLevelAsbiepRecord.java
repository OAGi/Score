/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.TopLevelAsbiep;


/**
 * This table indexes the ASBIEP which is a top-level ASBIEP. This table and the
 * owner_top_level_asbiep_id column in all BIE tables allow all related BIEs to
 * be retrieved all at once speeding up the profile BOD transactions.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class TopLevelAsbiepRecord extends UpdatableRecordImpl<TopLevelAsbiepRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.top_level_asbiep.top_level_asbiep_id</code>. A
     * internal, primary database key of an top-level ASBIEP.
     */
    public void setTopLevelAsbiepId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.top_level_asbiep_id</code>. A
     * internal, primary database key of an top-level ASBIEP.
     */
    public ULong getTopLevelAsbiepId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.based_top_level_asbiep_id</code>.
     * Foreign key referencing the inherited base TOP_LEVEL_ASBIEP_ID.
     */
    public void setBasedTopLevelAsbiepId(ULong value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.based_top_level_asbiep_id</code>.
     * Foreign key referencing the inherited base TOP_LEVEL_ASBIEP_ID.
     */
    public ULong getBasedTopLevelAsbiepId() {
        return (ULong) get(1);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.asbiep_id</code>. Foreign key to
     * the ASBIEP table pointing to a record which is a top-level ASBIEP.
     */
    public void setAsbiepId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.asbiep_id</code>. Foreign key to
     * the ASBIEP table pointing to a record which is a top-level ASBIEP.
     */
    public ULong getAsbiepId() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.owner_user_id</code>.
     */
    public void setOwnerUserId(ULong value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.owner_user_id</code>.
     */
    public ULong getOwnerUserId() {
        return (ULong) get(3);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.last_update_timestamp</code>. The
     * timestamp when among all related bie records was last updated.
     */
    public void setLastUpdateTimestamp(LocalDateTime value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.last_update_timestamp</code>. The
     * timestamp when among all related bie records was last updated.
     */
    public LocalDateTime getLastUpdateTimestamp() {
        return (LocalDateTime) get(4);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.last_updated_by</code>. A foreign
     * key referring to the last user who has updated any related bie records.
     */
    public void setLastUpdatedBy(ULong value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.last_updated_by</code>. A foreign
     * key referring to the last user who has updated any related bie records.
     */
    public ULong getLastUpdatedBy() {
        return (ULong) get(5);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.release_id</code>. Foreign key to
     * the RELEASE table. It identifies the release, for which this module is
     * associated.
     */
    public void setReleaseId(ULong value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.release_id</code>. Foreign key to
     * the RELEASE table. It identifies the release, for which this module is
     * associated.
     */
    public ULong getReleaseId() {
        return (ULong) get(6);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.version</code>. This column hold a
     * version number assigned by the user. This column is only used by the
     * top-level ASBIEP. No format of version is enforced.
     */
    public void setVersion(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.version</code>. This column hold a
     * version number assigned by the user. This column is only used by the
     * top-level ASBIEP. No format of version is enforced.
     */
    public String getVersion() {
        return (String) get(7);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.status</code>. This is different
     * from the STATE column which is CRUD life cycle of an entity. The use case
     * for this is to allow the user to indicate the usage status of a top-level
     * ASBIEP (a profile BOD). An integration architect can use this column.
     * Example values are ?Prototype?, ?Test?, and ?Production?. Only the
     * top-level ASBIEP can use this field.
     */
    public void setStatus(String value) {
        set(8, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.status</code>. This is different
     * from the STATE column which is CRUD life cycle of an entity. The use case
     * for this is to allow the user to indicate the usage status of a top-level
     * ASBIEP (a profile BOD). An integration architect can use this column.
     * Example values are ?Prototype?, ?Test?, and ?Production?. Only the
     * top-level ASBIEP can use this field.
     */
    public String getStatus() {
        return (String) get(8);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.state</code>.
     */
    public void setState(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.state</code>.
     */
    public String getState() {
        return (String) get(9);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.inverse_mode</code>. If this is
     * true, all BIEs not edited by users under this TOP_LEVEL_ASBIEP will be
     * treated as used BIEs.
     */
    public void setInverseMode(Byte value) {
        set(10, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.inverse_mode</code>. If this is
     * true, all BIEs not edited by users under this TOP_LEVEL_ASBIEP will be
     * treated as used BIEs.
     */
    public Byte getInverseMode() {
        return (Byte) get(10);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.is_deprecated</code>. Indicates
     * whether the TOP_LEVEL_ASBIEP is deprecated.
     */
    public void setIsDeprecated(Byte value) {
        set(11, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.is_deprecated</code>. Indicates
     * whether the TOP_LEVEL_ASBIEP is deprecated.
     */
    public Byte getIsDeprecated() {
        return (Byte) get(11);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.deprecated_reason</code>. The
     * reason for the deprecation of the TOP_LEVEL_ASBIEP.
     */
    public void setDeprecatedReason(String value) {
        set(12, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.deprecated_reason</code>. The
     * reason for the deprecation of the TOP_LEVEL_ASBIEP.
     */
    public String getDeprecatedReason() {
        return (String) get(12);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.deprecated_remark</code>. The
     * remark for the deprecation of the TOP_LEVEL_ASBIEP.
     */
    public void setDeprecatedRemark(String value) {
        set(13, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.deprecated_remark</code>. The
     * remark for the deprecation of the TOP_LEVEL_ASBIEP.
     */
    public String getDeprecatedRemark() {
        return (String) get(13);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.source_top_level_asbiep_id</code>.
     * A foreign key referring to the source TOP_LEVEL_ASBIEP_ID which has
     * linked to this record.
     */
    public void setSourceTopLevelAsbiepId(ULong value) {
        set(14, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.source_top_level_asbiep_id</code>.
     * A foreign key referring to the source TOP_LEVEL_ASBIEP_ID which has
     * linked to this record.
     */
    public ULong getSourceTopLevelAsbiepId() {
        return (ULong) get(14);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.source_action</code>. An action
     * that had used to create a reference from the source (e.g., 'Copy' or
     * 'Uplift'.)
     */
    public void setSourceAction(String value) {
        set(15, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.source_action</code>. An action
     * that had used to create a reference from the source (e.g., 'Copy' or
     * 'Uplift'.)
     */
    public String getSourceAction() {
        return (String) get(15);
    }

    /**
     * Setter for <code>oagi.top_level_asbiep.source_timestamp</code>. A
     * timestamp when a source reference had been made.
     */
    public void setSourceTimestamp(LocalDateTime value) {
        set(16, value);
    }

    /**
     * Getter for <code>oagi.top_level_asbiep.source_timestamp</code>. A
     * timestamp when a source reference had been made.
     */
    public LocalDateTime getSourceTimestamp() {
        return (LocalDateTime) get(16);
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
     * Create a detached TopLevelAsbiepRecord
     */
    public TopLevelAsbiepRecord() {
        super(TopLevelAsbiep.TOP_LEVEL_ASBIEP);
    }

    /**
     * Create a detached, initialised TopLevelAsbiepRecord
     */
    public TopLevelAsbiepRecord(ULong topLevelAsbiepId, ULong basedTopLevelAsbiepId, ULong asbiepId, ULong ownerUserId, LocalDateTime lastUpdateTimestamp, ULong lastUpdatedBy, ULong releaseId, String version, String status, String state, Byte inverseMode, Byte isDeprecated, String deprecatedReason, String deprecatedRemark, ULong sourceTopLevelAsbiepId, String sourceAction, LocalDateTime sourceTimestamp) {
        super(TopLevelAsbiep.TOP_LEVEL_ASBIEP);

        setTopLevelAsbiepId(topLevelAsbiepId);
        setBasedTopLevelAsbiepId(basedTopLevelAsbiepId);
        setAsbiepId(asbiepId);
        setOwnerUserId(ownerUserId);
        setLastUpdateTimestamp(lastUpdateTimestamp);
        setLastUpdatedBy(lastUpdatedBy);
        setReleaseId(releaseId);
        setVersion(version);
        setStatus(status);
        setState(state);
        setInverseMode(inverseMode);
        setIsDeprecated(isDeprecated);
        setDeprecatedReason(deprecatedReason);
        setDeprecatedRemark(deprecatedRemark);
        setSourceTopLevelAsbiepId(sourceTopLevelAsbiepId);
        setSourceAction(sourceAction);
        setSourceTimestamp(sourceTimestamp);
        resetTouchedOnNotNull();
    }
}
