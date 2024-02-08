/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.repo.api.impl.jooq.entity.tables.records;


import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.AgencyIdListManifest;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AgencyIdListManifestRecord extends UpdatableRecordImpl<AgencyIdListManifestRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for
     * <code>oagi.agency_id_list_manifest.agency_id_list_manifest_id</code>.
     */
    public void setAgencyIdListManifestId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for
     * <code>oagi.agency_id_list_manifest.agency_id_list_manifest_id</code>.
     */
    public ULong getAgencyIdListManifestId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.agency_id_list_manifest.release_id</code>.
     */
    public void setReleaseId(ULong value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.agency_id_list_manifest.release_id</code>.
     */
    public ULong getReleaseId() {
        return (ULong) get(1);
    }

    /**
     * Setter for <code>oagi.agency_id_list_manifest.agency_id_list_id</code>.
     */
    public void setAgencyIdListId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.agency_id_list_manifest.agency_id_list_id</code>.
     */
    public ULong getAgencyIdListId() {
        return (ULong) get(2);
    }

    /**
     * Setter for
     * <code>oagi.agency_id_list_manifest.agency_id_list_value_manifest_id</code>.
     */
    public void setAgencyIdListValueManifestId(ULong value) {
        set(3, value);
    }

    /**
     * Getter for
     * <code>oagi.agency_id_list_manifest.agency_id_list_value_manifest_id</code>.
     */
    public ULong getAgencyIdListValueManifestId() {
        return (ULong) get(3);
    }

    /**
     * Setter for
     * <code>oagi.agency_id_list_manifest.based_agency_id_list_manifest_id</code>.
     */
    public void setBasedAgencyIdListManifestId(ULong value) {
        set(4, value);
    }

    /**
     * Getter for
     * <code>oagi.agency_id_list_manifest.based_agency_id_list_manifest_id</code>.
     */
    public ULong getBasedAgencyIdListManifestId() {
        return (ULong) get(4);
    }

    /**
     * Setter for <code>oagi.agency_id_list_manifest.conflict</code>. This
     * indicates that there is a conflict between self and relationship.
     */
    public void setConflict(Byte value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.agency_id_list_manifest.conflict</code>. This
     * indicates that there is a conflict between self and relationship.
     */
    public Byte getConflict() {
        return (Byte) get(5);
    }

    /**
     * Setter for <code>oagi.agency_id_list_manifest.log_id</code>. A foreign
     * key pointed to a log for the current record.
     */
    public void setLogId(ULong value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.agency_id_list_manifest.log_id</code>. A foreign
     * key pointed to a log for the current record.
     */
    public ULong getLogId() {
        return (ULong) get(6);
    }

    /**
     * Setter for
     * <code>oagi.agency_id_list_manifest.replacement_agency_id_list_manifest_id</code>.
     * This refers to a replacement manifest if the record is deprecated.
     */
    public void setReplacementAgencyIdListManifestId(ULong value) {
        set(7, value);
    }

    /**
     * Getter for
     * <code>oagi.agency_id_list_manifest.replacement_agency_id_list_manifest_id</code>.
     * This refers to a replacement manifest if the record is deprecated.
     */
    public ULong getReplacementAgencyIdListManifestId() {
        return (ULong) get(7);
    }

    /**
     * Setter for
     * <code>oagi.agency_id_list_manifest.prev_agency_id_list_manifest_id</code>.
     */
    public void setPrevAgencyIdListManifestId(ULong value) {
        set(8, value);
    }

    /**
     * Getter for
     * <code>oagi.agency_id_list_manifest.prev_agency_id_list_manifest_id</code>.
     */
    public ULong getPrevAgencyIdListManifestId() {
        return (ULong) get(8);
    }

    /**
     * Setter for
     * <code>oagi.agency_id_list_manifest.next_agency_id_list_manifest_id</code>.
     */
    public void setNextAgencyIdListManifestId(ULong value) {
        set(9, value);
    }

    /**
     * Getter for
     * <code>oagi.agency_id_list_manifest.next_agency_id_list_manifest_id</code>.
     */
    public ULong getNextAgencyIdListManifestId() {
        return (ULong) get(9);
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
     * Create a detached AgencyIdListManifestRecord
     */
    public AgencyIdListManifestRecord() {
        super(AgencyIdListManifest.AGENCY_ID_LIST_MANIFEST);
    }

    /**
     * Create a detached, initialised AgencyIdListManifestRecord
     */
    public AgencyIdListManifestRecord(ULong agencyIdListManifestId, ULong releaseId, ULong agencyIdListId, ULong agencyIdListValueManifestId, ULong basedAgencyIdListManifestId, Byte conflict, ULong logId, ULong replacementAgencyIdListManifestId, ULong prevAgencyIdListManifestId, ULong nextAgencyIdListManifestId) {
        super(AgencyIdListManifest.AGENCY_ID_LIST_MANIFEST);

        setAgencyIdListManifestId(agencyIdListManifestId);
        setReleaseId(releaseId);
        setAgencyIdListId(agencyIdListId);
        setAgencyIdListValueManifestId(agencyIdListValueManifestId);
        setBasedAgencyIdListManifestId(basedAgencyIdListManifestId);
        setConflict(conflict);
        setLogId(logId);
        setReplacementAgencyIdListManifestId(replacementAgencyIdListManifestId);
        setPrevAgencyIdListManifestId(prevAgencyIdListManifestId);
        setNextAgencyIdListManifestId(nextAgencyIdListManifestId);
        resetChangedOnNotNull();
    }
}
