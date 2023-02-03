package org.oagi.score.e2e.api;

/**
 * APIs for the application settings.
 */
public interface ApplicationSettingsAPI {

    /**
     * Return {@code true} if the multi-tenant property is set to enable, otherwise {@code false}.
     *
     * @return {@code true} if the multi-tenant property is set to enable, otherwise {@code false}
     */
    boolean isTenantEnabled();

    /**
     * Set the multi-tenant property to either enable or disable.
     *
     * @param tenantEnable {@code true/false} for enable/disable
     */
    void setTenantEnable(boolean tenantEnable);

    /**
     * Return {@code true} if the business term property is set to enable, otherwise {@code false}.
     *
     * @return {@code true} if the business term property is set to enable, otherwise {@code false}
     */
    boolean isBusinessTermEnabled();

    /**
     * Set the business term property to either enable or disable.
     *
     * @param businessTermEnable {@code true/false} for enable/disable
     */
    void setBusinessTermEnable(boolean businessTermEnable);

}
