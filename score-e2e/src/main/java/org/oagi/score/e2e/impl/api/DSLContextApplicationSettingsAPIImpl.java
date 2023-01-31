package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.oagi.score.e2e.api.ApplicationSettingsAPI;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.CONFIGURATION;

public class DSLContextApplicationSettingsAPIImpl implements ApplicationSettingsAPI {

    private final DSLContext dslContext;

    public DSLContextApplicationSettingsAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public boolean isTenantEnabled() {
        return Boolean.valueOf(dslContext.select(CONFIGURATION.VALUE)
                .from(CONFIGURATION)
                .where(CONFIGURATION.NAME.eq("score.tenant.enabled"))
                .fetchOptionalInto(String.class).orElse("false"));
    }

    @Override
    public void setTenantEnable(boolean tenantEnable) {
        dslContext.update(CONFIGURATION)
                .set(CONFIGURATION.VALUE, (tenantEnable) ? "true" : "false")
                .where(CONFIGURATION.NAME.eq("score.tenant.enabled"))
                .execute();
    }

    @Override
    public boolean isBusinessTermEnabled() {
        return Boolean.valueOf(dslContext.select(CONFIGURATION.VALUE)
                .from(CONFIGURATION)
                .where(CONFIGURATION.NAME.eq("score.business-term.enabled"))
                .fetchOptionalInto(String.class).orElse("false"));
    }

    @Override
    public void setBusinessTermEnable(boolean businessTermEnable) {
        dslContext.update(CONFIGURATION)
                .set(CONFIGURATION.VALUE, (businessTermEnable) ? "true" : "false")
                .where(CONFIGURATION.NAME.eq("score.business-term.enabled"))
                .execute();
    }
}
