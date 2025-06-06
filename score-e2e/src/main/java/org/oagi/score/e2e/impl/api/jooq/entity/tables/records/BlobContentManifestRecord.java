/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.BlobContentManifest;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class BlobContentManifestRecord extends UpdatableRecordImpl<BlobContentManifestRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for
     * <code>oagi.blob_content_manifest.blob_content_manifest_id</code>.
     */
    public void setBlobContentManifestId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for
     * <code>oagi.blob_content_manifest.blob_content_manifest_id</code>.
     */
    public ULong getBlobContentManifestId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.blob_content_manifest.blob_content_id</code>.
     */
    public void setBlobContentId(ULong value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.blob_content_manifest.blob_content_id</code>.
     */
    public ULong getBlobContentId() {
        return (ULong) get(1);
    }

    /**
     * Setter for <code>oagi.blob_content_manifest.release_id</code>.
     */
    public void setReleaseId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.blob_content_manifest.release_id</code>.
     */
    public ULong getReleaseId() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.blob_content_manifest.conflict</code>. This
     * indicates that there is a conflict between self and relationship.
     */
    public void setConflict(Byte value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.blob_content_manifest.conflict</code>. This
     * indicates that there is a conflict between self and relationship.
     */
    public Byte getConflict() {
        return (Byte) get(3);
    }

    /**
     * Setter for
     * <code>oagi.blob_content_manifest.prev_blob_content_manifest_id</code>.
     */
    public void setPrevBlobContentManifestId(ULong value) {
        set(4, value);
    }

    /**
     * Getter for
     * <code>oagi.blob_content_manifest.prev_blob_content_manifest_id</code>.
     */
    public ULong getPrevBlobContentManifestId() {
        return (ULong) get(4);
    }

    /**
     * Setter for
     * <code>oagi.blob_content_manifest.next_blob_content_manifest_id</code>.
     */
    public void setNextBlobContentManifestId(ULong value) {
        set(5, value);
    }

    /**
     * Getter for
     * <code>oagi.blob_content_manifest.next_blob_content_manifest_id</code>.
     */
    public ULong getNextBlobContentManifestId() {
        return (ULong) get(5);
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
     * Create a detached BlobContentManifestRecord
     */
    public BlobContentManifestRecord() {
        super(BlobContentManifest.BLOB_CONTENT_MANIFEST);
    }

    /**
     * Create a detached, initialised BlobContentManifestRecord
     */
    public BlobContentManifestRecord(ULong blobContentManifestId, ULong blobContentId, ULong releaseId, Byte conflict, ULong prevBlobContentManifestId, ULong nextBlobContentManifestId) {
        super(BlobContentManifest.BLOB_CONTENT_MANIFEST);

        setBlobContentManifestId(blobContentManifestId);
        setBlobContentId(blobContentId);
        setReleaseId(releaseId);
        setConflict(conflict);
        setPrevBlobContentManifestId(prevBlobContentManifestId);
        setNextBlobContentManifestId(nextBlobContentManifestId);
        resetTouchedOnNotNull();
    }
}
