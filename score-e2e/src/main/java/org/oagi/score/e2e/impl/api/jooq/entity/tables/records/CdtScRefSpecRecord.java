/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.CdtScRefSpec;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CdtScRefSpecRecord extends UpdatableRecordImpl<CdtScRefSpecRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.cdt_sc_ref_spec.cdt_sc_ref_spec_id</code>.
     */
    public void setCdtScRefSpecId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.cdt_sc_ref_spec.cdt_sc_ref_spec_id</code>.
     */
    public ULong getCdtScRefSpecId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.cdt_sc_ref_spec.cdt_sc_id</code>.
     */
    public void setCdtScId(ULong value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.cdt_sc_ref_spec.cdt_sc_id</code>.
     */
    public ULong getCdtScId() {
        return (ULong) get(1);
    }

    /**
     * Setter for <code>oagi.cdt_sc_ref_spec.ref_spec_id</code>.
     */
    public void setRefSpecId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.cdt_sc_ref_spec.ref_spec_id</code>.
     */
    public ULong getRefSpecId() {
        return (ULong) get(2);
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
     * Create a detached CdtScRefSpecRecord
     */
    public CdtScRefSpecRecord() {
        super(CdtScRefSpec.CDT_SC_REF_SPEC);
    }

    /**
     * Create a detached, initialised CdtScRefSpecRecord
     */
    public CdtScRefSpecRecord(ULong cdtScRefSpecId, ULong cdtScId, ULong refSpecId) {
        super(CdtScRefSpec.CDT_SC_REF_SPEC);

        setCdtScRefSpecId(cdtScRefSpecId);
        setCdtScId(cdtScId);
        setRefSpecId(refSpecId);
        resetChangedOnNotNull();
    }
}
