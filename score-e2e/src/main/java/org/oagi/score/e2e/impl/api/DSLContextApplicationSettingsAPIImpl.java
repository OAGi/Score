package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.oagi.score.e2e.api.ApplicationSettingsAPI;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.CONFIGURATION;

public class DSLContextApplicationSettingsAPIImpl implements ApplicationSettingsAPI {

    private static final String BOOLEAN_TYPE = "Boolean";
    private static final String TENANT_ENABLED_CONFIG_NAME = "score.tenant.enabled";
    private static final String BUSINESS_TERM_ENABLED_CONFIG_NAME = "score.business-term.enabled";
    private static final String BROWSE_STANDARD_MODE_ENABLED_CONFIG_NAME = "score.browse-standard-mode.enabled";

    private final DSLContext dslContext;

    public DSLContextApplicationSettingsAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public boolean isTenantEnabled() {
        return getBooleanConfiguration(TENANT_ENABLED_CONFIG_NAME);
    }

    @Override
    public void setTenantEnable(boolean tenantEnable) {
        upsertBooleanConfiguration(TENANT_ENABLED_CONFIG_NAME, tenantEnable);
    }

    @Override
    public boolean isBusinessTermEnabled() {
        return getBooleanConfiguration(BUSINESS_TERM_ENABLED_CONFIG_NAME);
    }

    @Override
    public void setBusinessTermEnable(boolean businessTermEnable) {
        upsertBooleanConfiguration(BUSINESS_TERM_ENABLED_CONFIG_NAME, businessTermEnable);
    }

    @Override
    public boolean isBrowseStandardModeEnabled() {
        return getBooleanConfiguration(BROWSE_STANDARD_MODE_ENABLED_CONFIG_NAME);
    }

    @Override
    public void setBrowseStandardModeEnable(boolean browseStandardModeEnable) {
        upsertBooleanConfiguration(BROWSE_STANDARD_MODE_ENABLED_CONFIG_NAME, browseStandardModeEnable);
    }

    private boolean getBooleanConfiguration(String configurationName) {
        return Boolean.valueOf(dslContext.select(CONFIGURATION.VALUE)
                .from(CONFIGURATION)
                .where(CONFIGURATION.NAME.eq(configurationName))
                .fetchOptionalInto(String.class).orElse("false"));
    }

    private void upsertBooleanConfiguration(String configurationName, boolean enabled) {
        int updated = dslContext.update(CONFIGURATION)
                .set(CONFIGURATION.VALUE, enabled ? "true" : "false")
                .where(CONFIGURATION.NAME.eq(configurationName))
                .execute();
        if (updated == 0) {
            dslContext.insertInto(CONFIGURATION)
                    .set(CONFIGURATION.NAME, configurationName)
                    .set(CONFIGURATION.TYPE, BOOLEAN_TYPE)
                    .set(CONFIGURATION.VALUE, enabled ? "true" : "false")
                    .execute();
        }
    }
}
