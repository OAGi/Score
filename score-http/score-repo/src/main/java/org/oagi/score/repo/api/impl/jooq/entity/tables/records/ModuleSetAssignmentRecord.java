/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.repo.api.impl.jooq.entity.tables.records;


import java.time.LocalDateTime;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.ModuleSetAssignment;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ModuleSetAssignmentRecord extends UpdatableRecordImpl<ModuleSetAssignmentRecord> implements Record7<ULong, ULong, ULong, ULong, ULong, LocalDateTime, LocalDateTime> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.module_set_assignment.module_set_assignment_id</code>. Primary key.
     */
    public void setModuleSetAssignmentId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.module_set_assignment.module_set_assignment_id</code>. Primary key.
     */
    public ULong getModuleSetAssignmentId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.module_set_assignment.module_set_id</code>. A foreign key of the module set.
     */
    public void setModuleSetId(ULong value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.module_set_assignment.module_set_id</code>. A foreign key of the module set.
     */
    public ULong getModuleSetId() {
        return (ULong) get(1);
    }

    /**
     * Setter for <code>oagi.module_set_assignment.module_id</code>. A foreign key of the module assigned in the module set.
     */
    public void setModuleId(ULong value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.module_set_assignment.module_id</code>. A foreign key of the module assigned in the module set.
     */
    public ULong getModuleId() {
        return (ULong) get(2);
    }

    /**
     * Setter for <code>oagi.module_set_assignment.created_by</code>. Foreign key to the APP_USER table. It indicates the user who created this MODULE_SET_ASSIGNMENT.
     */
    public void setCreatedBy(ULong value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.module_set_assignment.created_by</code>. Foreign key to the APP_USER table. It indicates the user who created this MODULE_SET_ASSIGNMENT.
     */
    public ULong getCreatedBy() {
        return (ULong) get(3);
    }

    /**
     * Setter for <code>oagi.module_set_assignment.last_updated_by</code>. Foreign key to the APP_USER table referring to the last user who updated the record.
     */
    public void setLastUpdatedBy(ULong value) {
        set(4, value);
    }

    /**
     * Getter for <code>oagi.module_set_assignment.last_updated_by</code>. Foreign key to the APP_USER table referring to the last user who updated the record.
     */
    public ULong getLastUpdatedBy() {
        return (ULong) get(4);
    }

    /**
     * Setter for <code>oagi.module_set_assignment.creation_timestamp</code>. The timestamp when the record was first created.
     */
    public void setCreationTimestamp(LocalDateTime value) {
        set(5, value);
    }

    /**
     * Getter for <code>oagi.module_set_assignment.creation_timestamp</code>. The timestamp when the record was first created.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(5);
    }

    /**
     * Setter for <code>oagi.module_set_assignment.last_update_timestamp</code>. The timestamp when the record was last updated.
     */
    public void setLastUpdateTimestamp(LocalDateTime value) {
        set(6, value);
    }

    /**
     * Getter for <code>oagi.module_set_assignment.last_update_timestamp</code>. The timestamp when the record was last updated.
     */
    public LocalDateTime getLastUpdateTimestamp() {
        return (LocalDateTime) get(6);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<ULong> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row7<ULong, ULong, ULong, ULong, ULong, LocalDateTime, LocalDateTime> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    @Override
    public Row7<ULong, ULong, ULong, ULong, ULong, LocalDateTime, LocalDateTime> valuesRow() {
        return (Row7) super.valuesRow();
    }

    @Override
    public Field<ULong> field1() {
        return ModuleSetAssignment.MODULE_SET_ASSIGNMENT.MODULE_SET_ASSIGNMENT_ID;
    }

    @Override
    public Field<ULong> field2() {
        return ModuleSetAssignment.MODULE_SET_ASSIGNMENT.MODULE_SET_ID;
    }

    @Override
    public Field<ULong> field3() {
        return ModuleSetAssignment.MODULE_SET_ASSIGNMENT.MODULE_ID;
    }

    @Override
    public Field<ULong> field4() {
        return ModuleSetAssignment.MODULE_SET_ASSIGNMENT.CREATED_BY;
    }

    @Override
    public Field<ULong> field5() {
        return ModuleSetAssignment.MODULE_SET_ASSIGNMENT.LAST_UPDATED_BY;
    }

    @Override
    public Field<LocalDateTime> field6() {
        return ModuleSetAssignment.MODULE_SET_ASSIGNMENT.CREATION_TIMESTAMP;
    }

    @Override
    public Field<LocalDateTime> field7() {
        return ModuleSetAssignment.MODULE_SET_ASSIGNMENT.LAST_UPDATE_TIMESTAMP;
    }

    @Override
    public ULong component1() {
        return getModuleSetAssignmentId();
    }

    @Override
    public ULong component2() {
        return getModuleSetId();
    }

    @Override
    public ULong component3() {
        return getModuleId();
    }

    @Override
    public ULong component4() {
        return getCreatedBy();
    }

    @Override
    public ULong component5() {
        return getLastUpdatedBy();
    }

    @Override
    public LocalDateTime component6() {
        return getCreationTimestamp();
    }

    @Override
    public LocalDateTime component7() {
        return getLastUpdateTimestamp();
    }

    @Override
    public ULong value1() {
        return getModuleSetAssignmentId();
    }

    @Override
    public ULong value2() {
        return getModuleSetId();
    }

    @Override
    public ULong value3() {
        return getModuleId();
    }

    @Override
    public ULong value4() {
        return getCreatedBy();
    }

    @Override
    public ULong value5() {
        return getLastUpdatedBy();
    }

    @Override
    public LocalDateTime value6() {
        return getCreationTimestamp();
    }

    @Override
    public LocalDateTime value7() {
        return getLastUpdateTimestamp();
    }

    @Override
    public ModuleSetAssignmentRecord value1(ULong value) {
        setModuleSetAssignmentId(value);
        return this;
    }

    @Override
    public ModuleSetAssignmentRecord value2(ULong value) {
        setModuleSetId(value);
        return this;
    }

    @Override
    public ModuleSetAssignmentRecord value3(ULong value) {
        setModuleId(value);
        return this;
    }

    @Override
    public ModuleSetAssignmentRecord value4(ULong value) {
        setCreatedBy(value);
        return this;
    }

    @Override
    public ModuleSetAssignmentRecord value5(ULong value) {
        setLastUpdatedBy(value);
        return this;
    }

    @Override
    public ModuleSetAssignmentRecord value6(LocalDateTime value) {
        setCreationTimestamp(value);
        return this;
    }

    @Override
    public ModuleSetAssignmentRecord value7(LocalDateTime value) {
        setLastUpdateTimestamp(value);
        return this;
    }

    @Override
    public ModuleSetAssignmentRecord values(ULong value1, ULong value2, ULong value3, ULong value4, ULong value5, LocalDateTime value6, LocalDateTime value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ModuleSetAssignmentRecord
     */
    public ModuleSetAssignmentRecord() {
        super(ModuleSetAssignment.MODULE_SET_ASSIGNMENT);
    }

    /**
     * Create a detached, initialised ModuleSetAssignmentRecord
     */
    public ModuleSetAssignmentRecord(ULong moduleSetAssignmentId, ULong moduleSetId, ULong moduleId, ULong createdBy, ULong lastUpdatedBy, LocalDateTime creationTimestamp, LocalDateTime lastUpdateTimestamp) {
        super(ModuleSetAssignment.MODULE_SET_ASSIGNMENT);

        setModuleSetAssignmentId(moduleSetAssignmentId);
        setModuleSetId(moduleSetId);
        setModuleId(moduleId);
        setCreatedBy(createdBy);
        setLastUpdatedBy(lastUpdatedBy);
        setCreationTimestamp(creationTimestamp);
        setLastUpdateTimestamp(lastUpdateTimestamp);
    }
}
