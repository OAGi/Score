/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.AsccpManifest;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AsccpManifestRecord extends UpdatableRecordImpl<AsccpManifestRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.asccp_manifest.asccp_manifest_id</code>.
     */
    public void setAsccpManifestId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.asccp_manifest.asccp_manifest_id</code>.
     */
    public ULong getAsccpManifestId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.asccp_manifest.release_id</code>.
     */
    public void setReleaseId(ULong value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.asccp_manifest.release_id</code>.
     */
    public ULong getReleaseId() {
        return (ULong) get(1);
    }

    /**
     * Setter for <code>oagi.asccp_manifest.asccp_id</code>.
     */
    public void setAsccpId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.asccp_manifest.asccp_id</code>.
     */
    public ULong getAsccpId() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.asccp_manifest.role_of_acc_manifest_id</code>.
     */
    public void setRoleOfAccManifestId(ULong value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.asccp_manifest.role_of_acc_manifest_id</code>.
     */
    public ULong getRoleOfAccManifestId() {
        return (ULong) get(3);
    }

    /**
     * Setter for <code>oagi.asccp_manifest.den</code>. The dictionary entry
     * name of the ASCCP.
     */
    public void setDen(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.asccp_manifest.den</code>. The dictionary entry
     * name of the ASCCP.
     */
    public String getDen() {
        return (String) get(4);
    }

    /**
     * Setter for <code>oagi.asccp_manifest.conflict</code>. This indicates that
     * there is a conflict between self and relationship.
     */
    public void setConflict(Byte value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.asccp_manifest.conflict</code>. This indicates that
     * there is a conflict between self and relationship.
     */
    public Byte getConflict() {
        return (Byte) get(5);
    }

    /**
     * Setter for <code>oagi.asccp_manifest.log_id</code>. A foreign key pointed
     * to a log for the current record.
     */
    public void setLogId(ULong value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.asccp_manifest.log_id</code>. A foreign key pointed
     * to a log for the current record.
     */
    public ULong getLogId() {
        return (ULong) get(6);
    }

    /**
     * Setter for
     * <code>oagi.asccp_manifest.replacement_asccp_manifest_id</code>. This
     * refers to a replacement manifest if the record is deprecated.
     */
    public void setReplacementAsccpManifestId(ULong value) {
        set(7, value);
    }

    /**
     * Getter for
     * <code>oagi.asccp_manifest.replacement_asccp_manifest_id</code>. This
     * refers to a replacement manifest if the record is deprecated.
     */
    public ULong getReplacementAsccpManifestId() {
        return (ULong) get(7);
    }

    /**
     * Setter for <code>oagi.asccp_manifest.prev_asccp_manifest_id</code>.
     */
    public void setPrevAsccpManifestId(ULong value) {
        set(8, value);
    }

    /**
     * Getter for <code>oagi.asccp_manifest.prev_asccp_manifest_id</code>.
     */
    public ULong getPrevAsccpManifestId() {
        return (ULong) get(8);
    }

    /**
     * Setter for <code>oagi.asccp_manifest.next_asccp_manifest_id</code>.
     */
    public void setNextAsccpManifestId(ULong value) {
        set(9, value);
    }

    /**
     * Getter for <code>oagi.asccp_manifest.next_asccp_manifest_id</code>.
     */
    public ULong getNextAsccpManifestId() {
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
     * Create a detached AsccpManifestRecord
     */
    public AsccpManifestRecord() {
        super(AsccpManifest.ASCCP_MANIFEST);
    }

    /**
     * Create a detached, initialised AsccpManifestRecord
     */
    public AsccpManifestRecord(ULong asccpManifestId, ULong releaseId, ULong asccpId, ULong roleOfAccManifestId, String den, Byte conflict, ULong logId, ULong replacementAsccpManifestId, ULong prevAsccpManifestId, ULong nextAsccpManifestId) {
        super(AsccpManifest.ASCCP_MANIFEST);

        setAsccpManifestId(asccpManifestId);
        setReleaseId(releaseId);
        setAsccpId(asccpId);
        setRoleOfAccManifestId(roleOfAccManifestId);
        setDen(den);
        setConflict(conflict);
        setLogId(logId);
        setReplacementAsccpManifestId(replacementAsccpManifestId);
        setPrevAsccpManifestId(prevAsccpManifestId);
        setNextAsccpManifestId(nextAsccpManifestId);
        resetChangedOnNotNull();
    }
}
