/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.e2e.impl.api.jooq.entity.tables.records;


import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Configuration;


/**
 * The table stores configuration properties of the application.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ConfigurationRecord extends UpdatableRecordImpl<ConfigurationRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>oagi.configuration.configuration_id</code>. Primary key
     * column.
     */
    public void setConfigurationId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>oagi.configuration.configuration_id</code>. Primary key
     * column.
     */
    public ULong getConfigurationId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>oagi.configuration.name</code>. The name of
     * configuration property.
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>oagi.configuration.name</code>. The name of
     * configuration property.
     */
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>oagi.configuration.type</code>. The type of
     * configuration property.
     */
    public void setType(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>oagi.configuration.type</code>. The type of
     * configuration property.
     */
    public String getType() {
        return (String) get(2);
    }

    /**
     * Setter for <code>oagi.configuration.value</code>. The value of
     * configuration property.
     */
    public void setValue(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>oagi.configuration.value</code>. The value of
     * configuration property.
     */
    public String getValue() {
        return (String) get(3);
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
     * Create a detached ConfigurationRecord
     */
    public ConfigurationRecord() {
        super(Configuration.CONFIGURATION);
    }

    /**
     * Create a detached, initialised ConfigurationRecord
     */
    public ConfigurationRecord(ULong configurationId, String name, String type, String value) {
        super(Configuration.CONFIGURATION);

        setConfigurationId(configurationId);
        setName(name);
        setType(type);
        setValue(value);
        resetChangedOnNotNull();
    }
}
