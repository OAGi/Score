package org.oagi.score.e2e.page.oas;

import org.oagi.score.e2e.page.Dialog;

import java.util.List;

/**
 * The 'Add Security Scheme' / 'Edit Security Scheme' dialog (Issue #1729). It edits a single OpenAPI
 * Security Scheme whose visible fields depend on the chosen Type (apiKey | http | oauth2 |
 * openIdConnect). The dialog edits a working copy and is confirmed with 'Add' (new) or 'Save' (edit);
 * the document is only persisted later by the page's Update/Generate action.
 */
public interface OasSecuritySchemeDialog extends Dialog {

    /**
     * Select the scheme Type by its display label ('API Key', 'HTTP', 'OAuth 2.0', 'OpenID Connect').
     *
     * @param typeLabel the type label
     */
    void setType(String typeLabel);

    /**
     * Return the currently selected Type label.
     *
     * @return the Type label
     */
    String getType();

    /**
     * Return the available Type option labels.
     *
     * @return the Type option labels
     */
    List<String> getTypeOptions();

    /**
     * Return the current 'Scheme Name' value.
     *
     * @return the Scheme Name
     */
    String getSchemeName();

    /**
     * Set the 'Scheme Name' field.
     *
     * @param schemeName the Scheme Name
     */
    void setSchemeName(String schemeName);

    /**
     * Set the 'Description (optional)' field.
     *
     * @param description the description
     */
    void setDescription(String description);

    /* --------------------------------------------------------------- apiKey */

    /**
     * Return whether the apiKey 'In' selector is displayed.
     *
     * @return {@code true} when displayed
     */
    boolean isInFieldDisplayed();

    /**
     * Return the apiKey 'In' option labels ('Query', 'Header', 'Cookie').
     *
     * @return the In option labels
     */
    List<String> getInOptions();

    /**
     * Select the apiKey 'In' by its label ('Query', 'Header', 'Cookie').
     *
     * @param inLabel the In label
     */
    void setIn(String inLabel);

    /**
     * Return the apiKey 'Name' value.
     *
     * @return the apiKey Name
     */
    String getApiKeyName();

    /**
     * Set the apiKey 'Name' field.
     *
     * @param name the apiKey Name
     */
    void setApiKeyName(String name);

    /**
     * Clear the apiKey 'Name' field.
     */
    void clearApiKeyName();

    /* ----------------------------------------------------------------- http */

    /**
     * Select the http 'Scheme' by its label ('Basic', 'Bearer').
     *
     * @param schemeLabel the http scheme label
     */
    void setHttpScheme(String schemeLabel);

    /**
     * Return whether the http 'Bearer Format' field is displayed.
     *
     * @return {@code true} when displayed
     */
    boolean isBearerFormatFieldDisplayed();

    /* -------------------------------------------------------- openIdConnect */

    /**
     * Return whether the 'OpenID Connect URL' field is displayed.
     *
     * @return {@code true} when displayed
     */
    boolean isOpenIdConnectUrlFieldDisplayed();

    /**
     * Return the 'OpenID Connect URL' value.
     *
     * @return the OpenID Connect URL
     */
    String getOpenIdConnectUrl();

    /**
     * Set the 'OpenID Connect URL' field.
     *
     * @param url the OpenID Connect URL
     */
    void setOpenIdConnectUrl(String url);

    /**
     * Clear the 'OpenID Connect URL' field.
     */
    void clearOpenIdConnectUrl();

    /* --------------------------------------------------------------- oauth2 */

    /**
     * Return whether the 'OAuth Flows' editor section is displayed.
     *
     * @return {@code true} when displayed
     */
    boolean isOAuthFlowsSectionDisplayed();

    /**
     * Return the number of OAuth Flow cards.
     *
     * @return the flow-card count
     */
    int getFlowCount();

    /**
     * Return the Flow Type label shown on a flow card.
     *
     * @param flowIndex the 1-based flow-card index
     * @return the Flow Type label
     */
    String getFlowType(int flowIndex);

    /**
     * Return whether a flow card displays an 'Authorization URL' field.
     *
     * @param flowIndex the 1-based flow-card index
     * @return {@code true} when displayed
     */
    boolean isFlowAuthorizationUrlDisplayed(int flowIndex);

    /**
     * Return whether a flow card displays a 'Token URL' field.
     *
     * @param flowIndex the 1-based flow-card index
     * @return {@code true} when displayed
     */
    boolean isFlowTokenUrlDisplayed(int flowIndex);

    /**
     * Return the scope names declared on a flow card.
     *
     * @param flowIndex the 1-based flow-card index
     * @return the scope names
     */
    List<String> getFlowScopeNames(int flowIndex);

    /**
     * Click 'Add Flow'.
     */
    void addFlow();

    /**
     * Click 'Add Scope' on a flow card.
     *
     * @param flowIndex the 1-based flow-card index
     */
    void addScope(int flowIndex);

    /**
     * Remove the last scope row of a flow card.
     *
     * @param flowIndex the 1-based flow-card index
     */
    void removeLastScope(int flowIndex);

    /* --------------------------------------------------------------- actions */

    /**
     * Return whether the primary ('Add' / 'Save') button is enabled.
     *
     * @return {@code true} when enabled
     */
    boolean isPrimaryButtonEnabled();

    /**
     * Confirm the dialog by clicking its primary ('Add' / 'Save') button and wait for it to close.
     */
    void hitPrimaryButton();

    /**
     * Cancel (close) the dialog.
     */
    void cancel();
}
