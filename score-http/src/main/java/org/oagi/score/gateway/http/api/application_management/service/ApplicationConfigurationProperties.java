package org.oagi.score.gateway.http.api.application_management.service;

public final class ApplicationConfigurationProperties {

    private ApplicationConfigurationProperties() {
    }

    public static final String TENANT_CONFIG_PARAM_NAME = "score.tenant.enabled";

    public static final String BUSINESS_TERM_CONFIG_PARAM_NAME = "score.business-term.enabled";

    public static final String BIE_INVERSE_MODE_CONFIG_PARAM_NAME = "score.bie.inverse-mode";

    public static final String FUNCTIONS_REQUIRING_EMAIL_TRANSMISSION_CONFIG_PARAM_NAME =
            "score.functions-requiring-email-transmission.enabled";

    public static final String BROWSE_STANDARD_MODE_CONFIG_PARAM_NAME =
            "score.browse-standard-mode.enabled";

    public static final String BIE_SCHEMA_FILENAME_EXPRESSION_CONFIG_PARAM_NAME =
            "score.bie.schema-filename-expression";

    public static final String BIE_PACKAGE_SCHEMA_FILENAME_EXPRESSION_CONFIG_PARAM_NAME =
            "score.bie.package-schema-filename-expression";

    public static final String BIE_SCHEMA_FILENAME_DUPLICATE_HANDLER_EXPRESSION_CONFIG_PARAM_NAME =
            "score.bie.schema-filename-duplicate-handler-expression";

    public static final String BIE_PACKAGE_SCHEMA_FILENAME_DUPLICATE_HANDLER_EXPRESSION_CONFIG_PARAM_NAME =
            "score.bie.package-schema-filename-duplicate-handler-expression";

    public static final String NAVBAR_BRAND_CONFIG_PARAM_NAME = "score.pages.navbar.brand";

    public static final String FAVICON_LINK_CONFIG_PARAM_NAME = "score.pages.favicon.link";

    public static final String SIGN_IN_PAGE_STATEMENT_CONFIG_PARAM_NAME = "score.pages.signin.statement";

    public static String COMPONENT_STATE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(String state) {
        return "score.pages.colors.cc-state." + state + ".background";
    }

    public static String COMPONENT_STATE_FONT_COLOR_CONFIG_PARAM_NAME(String state) {
        return "score.pages.colors.cc-state." + state + ".font";
    }

    public static String RELEASE_STATE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(String state) {
        return "score.pages.colors.release-state." + state + ".background";
    }

    public static String RELEASE_STATE_FONT_COLOR_CONFIG_PARAM_NAME(String state) {
        return "score.pages.colors.release-state." + state + ".font";
    }

    public static String USER_ROLE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(String role) {
        return "score.pages.colors.user-role." + role + ".background";
    }

    public static String USER_ROLE_FONT_COLOR_CONFIG_PARAM_NAME(String role) {
        return "score.pages.colors.user-role." + role + ".font";
    }

}
