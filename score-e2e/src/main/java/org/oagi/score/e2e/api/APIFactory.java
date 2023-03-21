package org.oagi.score.e2e.api;

/**
 * API Accessor
 */
public interface APIFactory extends AutoCloseable {

    /**
     * Return the application settings API.
     *
     * @return the application settings API
     */
    ApplicationSettingsAPI getApplicationSettingsAPI();

    /**
     * Return the account management API.
     *
     * @return the account management API
     */
    AppUserAPI getAppUserAPI();

    /**
     * Return the namespace management API.
     *
     * @return the namespace management API
     */
    NamespaceAPI getNamespaceAPI();

    /**
     * Return the context category management API.
     *
     * @return the context category management API
     */
    ContextCategoryAPI getContextCategoryAPI();

    /**
     * Return the context scheme management API.
     *
     * @return the context scheme management API
     */
    ContextSchemeAPI getContextSchemeAPI();

    /**
     * Return the context scheme value management API.
     *
     * @return the context scheme value management API
     */
    ContextSchemeValueAPI getContextSchemeValueAPI();

    /**
     * Return the business context management API.
     *
     * @return the business context management API
     */
    BusinessContextAPI getBusinessContextAPI();

    /**
     * Return the business context value management API.
     *
     * @return the business context value management API
     */
    BusinessContextValueAPI getBusinessContextValueAPI();

    /**
     * Return the business term management API.
     *
     * @return the business term management API
     */
    BusinessTermAPI getBusinessTermAPI();

    /**
     * Return the assigned business term API
     * @return the assigned business term API
     */
    AssignedBusinessTermAPI getAssignedBusinessTermAPI();

    /**
     * Return the agency ID list management API.
     *
     * @return the agency ID list management API
     */
    AgencyIDListAPI getAgencyIDListAPI();

    /**
     * Return the agency ID list value management API.
     *
     * @return the agency ID list value management API
     */
    AgencyIDListValueAPI getAgencyIDListValueAPI();

    /**
     * Return the code list management API.
     *
     * @return the code list management API
     */
    CodeListAPI getCodeListAPI();

    /**
     * Return the code list value management API.
     *
     * @return the code list value management API
     */
    CodeListValueAPI getCodeListValueAPI();

    /**
     * Return the core component management API.
     *
     * @return the core component management API
     */
    CoreComponentAPI getCoreComponentAPI();

    /**
     * Return the release management API.
     *
     * @return the release management API
     */
    ReleaseAPI getReleaseAPI();

    /**
     * Return the business information entity (BIE) management API.
     *
     * @return the business information entity (BIE) management API
     */
    BusinessInformationEntityAPI getBusinessInformationEntityAPI();

}
