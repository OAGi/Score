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
import org.oagi.score.repo.api.impl.jooq.entity.tables.AccManifest;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AccManifestRecord extends UpdatableRecordImpl<AccManifestRecord> implements Record9<ULong, ULong, ULong, ULong, Byte, ULong, ULong, ULong, ULong> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.acc_manifest.acc_manifest_id</code>.
     */
    public void setAccManifestId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.acc_manifest.acc_manifest_id</code>.
     */
    public ULong getAccManifestId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.acc_manifest.release_id</code>.
     */
    public void setReleaseId(ULong value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.acc_manifest.release_id</code>.
     */
    public ULong getReleaseId() {
        return (ULong) get(1);
    }

    /**
     * Setter for <code>oagi.acc_manifest.acc_id</code>.
     */
    public void setAccId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.acc_manifest.acc_id</code>.
     */
    public ULong getAccId() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.acc_manifest.based_acc_manifest_id</code>.
     */
    public void setBasedAccManifestId(ULong value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.acc_manifest.based_acc_manifest_id</code>.
     */
    public ULong getBasedAccManifestId() {
        return (ULong) get(3);
    }

    /**
     * Setter for <code>oagi.acc_manifest.conflict</code>. This indicates that there is a conflict between self and relationship.
     */
    public void setConflict(Byte value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.acc_manifest.conflict</code>. This indicates that there is a conflict between self and relationship.
     */
    public Byte getConflict() {
        return (Byte) get(4);
    }

    /**
     * Setter for <code>oagi.acc_manifest.log_id</code>. A foreign key pointed to a log for the current record.
     */
    public void setLogId(ULong value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.acc_manifest.log_id</code>. A foreign key pointed to a log for the current record.
     */
    public ULong getLogId() {
        return (ULong) get(5);
    }

    /**
     * Setter for <code>oagi.acc_manifest.replacement_acc_manifest_id</code>. This refers to a replacement manifest if the record is deprecated.
     */
    public void setReplacementAccManifestId(ULong value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.acc_manifest.replacement_acc_manifest_id</code>. This refers to a replacement manifest if the record is deprecated.
     */
    public ULong getReplacementAccManifestId() {
        return (ULong) get(6);
    }

    /**
     * Setter for <code>oagi.acc_manifest.prev_acc_manifest_id</code>.
     */
    public void setPrevAccManifestId(ULong value) {
        set(7, value);
    }

    /**
     * Getter for <code>oagi.acc_manifest.prev_acc_manifest_id</code>.
     */
    public ULong getPrevAccManifestId() {
        return (ULong) get(7);
    }

    /**
     * Setter for <code>oagi.acc_manifest.next_acc_manifest_id</code>.
     */
    public void setNextAccManifestId(ULong value) {
        set(8, value);
    }

    /**
     * Getter for <code>oagi.acc_manifest.next_acc_manifest_id</code>.
     */
    public ULong getNextAccManifestId() {
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
        return AccManifest.ACC_MANIFEST.ACC_MANIFEST_ID;
    }

    @Override
    public Field<ULong> field2() {
        return AccManifest.ACC_MANIFEST.RELEASE_ID;
    }

    @Override
    public Field<ULong> field3() {
        return AccManifest.ACC_MANIFEST.ACC_ID;
    }

    @Override
    public Field<ULong> field4() {
        return AccManifest.ACC_MANIFEST.BASED_ACC_MANIFEST_ID;
    }

    @Override
    public Field<Byte> field5() {
        return AccManifest.ACC_MANIFEST.CONFLICT;
    }

    @Override
    public Field<ULong> field6() {
        return AccManifest.ACC_MANIFEST.LOG_ID;
    }

    @Override
    public Field<ULong> field7() {
        return AccManifest.ACC_MANIFEST.REPLACEMENT_ACC_MANIFEST_ID;
    }

    @Override
    public Field<ULong> field8() {
        return AccManifest.ACC_MANIFEST.PREV_ACC_MANIFEST_ID;
    }

    @Override
    public Field<ULong> field9() {
        return AccManifest.ACC_MANIFEST.NEXT_ACC_MANIFEST_ID;
    }

    @Override
    public ULong component1() {
        return getAccManifestId();
    }

    @Override
    public ULong component2() {
        return getReleaseId();
    }

    @Override
    public ULong component3() {
        return getAccId();
    }

    @Override
    public ULong component4() {
        return getBasedAccManifestId();
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
        return getReplacementAccManifestId();
    }

    @Override
    public ULong component8() {
        return getPrevAccManifestId();
    }

    @Override
    public ULong component9() {
        return getNextAccManifestId();
    }

    @Override
    public ULong value1() {
        return getAccManifestId();
    }

    @Override
    public ULong value2() {
        return getReleaseId();
    }

    @Override
    public ULong value3() {
        return getAccId();
    }

    @Override
    public ULong value4() {
        return getBasedAccManifestId();
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
        return getReplacementAccManifestId();
    }

    @Override
    public ULong value8() {
        return getPrevAccManifestId();
    }

    @Override
    public ULong value9() {
        return getNextAccManifestId();
    }

    @Override
    public AccManifestRecord value1(ULong value) {
        setAccManifestId(value);
        return this;
    }

    @Override
    public AccManifestRecord value2(ULong value) {
        setReleaseId(value);
        return this;
    }

    @Override
    public AccManifestRecord value3(ULong value) {
        setAccId(value);
        return this;
    }

    @Override
    public AccManifestRecord value4(ULong value) {
        setBasedAccManifestId(value);
        return this;
    }

    @Override
    public AccManifestRecord value5(Byte value) {
        setConflict(value);
        return this;
    }

    @Override
    public AccManifestRecord value6(ULong value) {
        setLogId(value);
        return this;
    }

    @Override
    public AccManifestRecord value7(ULong value) {
        setReplacementAccManifestId(value);
        return this;
    }

    @Override
    public AccManifestRecord value8(ULong value) {
        setPrevAccManifestId(value);
        return this;
    }

    @Override
    public AccManifestRecord value9(ULong value) {
        setNextAccManifestId(value);
        return this;
    }

    @Override
    public AccManifestRecord values(ULong value1, ULong value2, ULong value3, ULong value4, Byte value5, ULong value6, ULong value7, ULong value8, ULong value9) {
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
     * Create a detached AccManifestRecord
     */
    public AccManifestRecord() {
        super(AccManifest.ACC_MANIFEST);
    }

    /**
     * Create a detached, initialised AccManifestRecord
     */
    public AccManifestRecord(ULong accManifestId, ULong releaseId, ULong accId, ULong basedAccManifestId, Byte conflict, ULong logId, ULong replacementAccManifestId, ULong prevAccManifestId, ULong nextAccManifestId) {
        super(AccManifest.ACC_MANIFEST);

        setAccManifestId(accManifestId);
        setReleaseId(releaseId);
        setAccId(accId);
        setBasedAccManifestId(basedAccManifestId);
        setConflict(conflict);
        setLogId(logId);
        setReplacementAccManifestId(replacementAccManifestId);
        setPrevAccManifestId(prevAccManifestId);
        setNextAccManifestId(nextAccManifestId);
    }
}
