/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.repo.api.impl.jooq.entity.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record9;
import org.jooq.Row9;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.DtManifest;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DtManifestRecord extends UpdatableRecordImpl<DtManifestRecord> implements Record9<ULong, ULong, ULong, ULong, Byte, ULong, ULong, ULong, ULong> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.dt_manifest.dt_manifest_id</code>.
     */
    public void setDtManifestId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.dt_manifest.dt_manifest_id</code>.
     */
    public ULong getDtManifestId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.dt_manifest.release_id</code>.
     */
    public void setReleaseId(ULong value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.dt_manifest.release_id</code>.
     */
    public ULong getReleaseId() {
        return (ULong) get(1);
    }

    /**
     * Setter for <code>oagi.dt_manifest.dt_id</code>.
     */
    public void setDtId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.dt_manifest.dt_id</code>.
     */
    public ULong getDtId() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.dt_manifest.based_dt_manifest_id</code>.
     */
    public void setBasedDtManifestId(ULong value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.dt_manifest.based_dt_manifest_id</code>.
     */
    public ULong getBasedDtManifestId() {
        return (ULong) get(3);
    }

    /**
     * Setter for <code>oagi.dt_manifest.conflict</code>. This indicates that there is a conflict between self and relationship.
     */
    public void setConflict(Byte value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.dt_manifest.conflict</code>. This indicates that there is a conflict between self and relationship.
     */
    public Byte getConflict() {
        return (Byte) get(4);
    }

    /**
     * Setter for <code>oagi.dt_manifest.log_id</code>. A foreign key pointed to a log for the current record.
     */
    public void setLogId(ULong value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.dt_manifest.log_id</code>. A foreign key pointed to a log for the current record.
     */
    public ULong getLogId() {
        return (ULong) get(5);
    }

    /**
     * Setter for <code>oagi.dt_manifest.replacement_dt_manifest_id</code>. This refers to a replacement manifest if the record is deprecated.
     */
    public void setReplacementDtManifestId(ULong value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.dt_manifest.replacement_dt_manifest_id</code>. This refers to a replacement manifest if the record is deprecated.
     */
    public ULong getReplacementDtManifestId() {
        return (ULong) get(6);
    }

    /**
     * Setter for <code>oagi.dt_manifest.prev_dt_manifest_id</code>.
     */
    public void setPrevDtManifestId(ULong value) {
        set(7, value);
    }

    /**
     * Getter for <code>oagi.dt_manifest.prev_dt_manifest_id</code>.
     */
    public ULong getPrevDtManifestId() {
        return (ULong) get(7);
    }

    /**
     * Setter for <code>oagi.dt_manifest.next_dt_manifest_id</code>.
     */
    public void setNextDtManifestId(ULong value) {
        set(8, value);
    }

    /**
     * Getter for <code>oagi.dt_manifest.next_dt_manifest_id</code>.
     */
    public ULong getNextDtManifestId() {
        return (ULong) get(8);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<ULong> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record9 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row9<ULong, ULong, ULong, ULong, Byte, ULong, ULong, ULong, ULong> fieldsRow() {
        return (Row9) super.fieldsRow();
    }

    @Override
    public Row9<ULong, ULong, ULong, ULong, Byte, ULong, ULong, ULong, ULong> valuesRow() {
        return (Row9) super.valuesRow();
    }

    @Override
    public Field<ULong> field1() {
        return DtManifest.DT_MANIFEST.DT_MANIFEST_ID;
    }

    @Override
    public Field<ULong> field2() {
        return DtManifest.DT_MANIFEST.RELEASE_ID;
    }

    @Override
    public Field<ULong> field3() {
        return DtManifest.DT_MANIFEST.DT_ID;
    }

    @Override
    public Field<ULong> field4() {
        return DtManifest.DT_MANIFEST.BASED_DT_MANIFEST_ID;
    }

    @Override
    public Field<Byte> field5() {
        return DtManifest.DT_MANIFEST.CONFLICT;
    }

    @Override
    public Field<ULong> field6() {
        return DtManifest.DT_MANIFEST.LOG_ID;
    }

    @Override
    public Field<ULong> field7() {
        return DtManifest.DT_MANIFEST.REPLACEMENT_DT_MANIFEST_ID;
    }

    @Override
    public Field<ULong> field8() {
        return DtManifest.DT_MANIFEST.PREV_DT_MANIFEST_ID;
    }

    @Override
    public Field<ULong> field9() {
        return DtManifest.DT_MANIFEST.NEXT_DT_MANIFEST_ID;
    }

    @Override
    public ULong component1() {
        return getDtManifestId();
    }

    @Override
    public ULong component2() {
        return getReleaseId();
    }

    @Override
    public ULong component3() {
        return getDtId();
    }

    @Override
    public ULong component4() {
        return getBasedDtManifestId();
    }

    @Override
    public Byte component5() {
        return getConflict();
    }

    @Override
    public ULong component6() {
        return getLogId();
    }

    @Override
    public ULong component7() {
        return getReplacementDtManifestId();
    }

    @Override
    public ULong component8() {
        return getPrevDtManifestId();
    }

    @Override
    public ULong component9() {
        return getNextDtManifestId();
    }

    @Override
    public ULong value1() {
        return getDtManifestId();
    }

    @Override
    public ULong value2() {
        return getReleaseId();
    }

    @Override
    public ULong value3() {
        return getDtId();
    }

    @Override
    public ULong value4() {
        return getBasedDtManifestId();
    }

    @Override
    public Byte value5() {
        return getConflict();
    }

    @Override
    public ULong value6() {
        return getLogId();
    }

    @Override
    public ULong value7() {
        return getReplacementDtManifestId();
    }

    @Override
    public ULong value8() {
        return getPrevDtManifestId();
    }

    @Override
    public ULong value9() {
        return getNextDtManifestId();
    }

    @Override
    public DtManifestRecord value1(ULong value) {
        setDtManifestId(value);
        return this;
    }

    @Override
    public DtManifestRecord value2(ULong value) {
        setReleaseId(value);
        return this;
    }

    @Override
    public DtManifestRecord value3(ULong value) {
        setDtId(value);
        return this;
    }

    @Override
    public DtManifestRecord value4(ULong value) {
        setBasedDtManifestId(value);
        return this;
    }

    @Override
    public DtManifestRecord value5(Byte value) {
        setConflict(value);
        return this;
    }

    @Override
    public DtManifestRecord value6(ULong value) {
        setLogId(value);
        return this;
    }

    @Override
    public DtManifestRecord value7(ULong value) {
        setReplacementDtManifestId(value);
        return this;
    }

    @Override
    public DtManifestRecord value8(ULong value) {
        setPrevDtManifestId(value);
        return this;
    }

    @Override
    public DtManifestRecord value9(ULong value) {
        setNextDtManifestId(value);
        return this;
    }

    @Override
    public DtManifestRecord values(ULong value1, ULong value2, ULong value3, ULong value4, Byte value5, ULong value6, ULong value7, ULong value8, ULong value9) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached DtManifestRecord
     */
    public DtManifestRecord() {
        super(DtManifest.DT_MANIFEST);
    }

    /**
     * Create a detached, initialised DtManifestRecord
     */
    public DtManifestRecord(ULong dtManifestId, ULong releaseId, ULong dtId, ULong basedDtManifestId, Byte conflict, ULong logId, ULong replacementDtManifestId, ULong prevDtManifestId, ULong nextDtManifestId) {
        super(DtManifest.DT_MANIFEST);

        setDtManifestId(dtManifestId);
        setReleaseId(releaseId);
        setDtId(dtId);
        setBasedDtManifestId(basedDtManifestId);
        setConflict(conflict);
        setLogId(logId);
        setReplacementDtManifestId(replacementDtManifestId);
        setPrevDtManifestId(prevDtManifestId);
        setNextDtManifestId(nextDtManifestId);
    }
}
