package org.oagi.score.e2e.page.oas;

import org.oagi.score.e2e.page.Dialog;

import java.util.List;

/**
 * The 'Document Security' / 'Operation Security' requirement editor dialog (Issue #1729). It edits a
 * {@code security} array (a list of Security Requirement Objects): each requirement card is one OR
 * alternative, the schemes within a card are ANDed, and a card may be marked anonymous ({@code {}}).
 * For an operation the dialog additionally offers Inherit / No-security (public) / Override modes.
 * Confirmed with 'Apply'; the document is persisted later by the page's Update/Generate action.
 */
public interface OasSecurityRequirementDialog extends Dialog {

    /**
     * Return whether the operation-mode radio group ('Use document security' / 'No security...' /
     * 'Override...') is displayed. It is present only for the per-operation dialog.
     *
     * @return {@code true} when displayed
     */
    boolean hasModeRadioGroup();

    /**
     * Select the operation security mode by its radio label.
     *
     * @param modeLabel one of 'Use document security', 'No security for this operation',
     *                  'Override with selected schemes'
     */
    void setMode(String modeLabel);

    /**
     * Return the number of requirement cards (OR alternatives) currently shown.
     *
     * @return the requirement-card count
     */
    int getRequirementCount();

    /**
     * Select the Security Scheme of an AND row within a requirement card.
     *
     * @param cardIndex  the 1-based requirement-card index
     * @param rowIndex   the 1-based AND-row index within the card
     * @param schemeName the scheme name to select (matched by its leading label)
     */
    void setRequirementScheme(int cardIndex, int rowIndex, String schemeName);

    /**
     * Select oauth2 scopes (a multi-select) for an AND row within a requirement card.
     *
     * @param cardIndex the 1-based requirement-card index
     * @param rowIndex  the 1-based AND-row index within the card
     * @param scopes    the scope labels to select
     */
    void setRequirementScopes(int cardIndex, int rowIndex, List<String> scopes);

    /**
     * Click 'Add scheme (AND)' on a requirement card to add another ANDed scheme.
     *
     * @param cardIndex the 1-based requirement-card index
     */
    void addAndScheme(int cardIndex);

    /**
     * Click 'Add Alternative (OR)' to add another OR alternative requirement card.
     */
    void addAlternative();

    /**
     * Set the 'Allow anonymous ({})' checkbox on a requirement card.
     *
     * @param cardIndex the 1-based requirement-card index
     * @param checked   {@code true} to allow anonymous
     */
    void setAnonymous(int cardIndex, boolean checked);

    /**
     * Return whether any duplicate-requirement warning is currently displayed.
     *
     * @return {@code true} when a duplicate warning is shown
     */
    boolean isDuplicateWarningDisplayed();

    /**
     * Return the text of the first duplicate-requirement warning, or an empty string when none.
     *
     * @return the duplicate warning text
     */
    String getDuplicateWarningText();

    /**
     * Return whether the 'Apply' button is enabled.
     *
     * @return {@code true} when enabled
     */
    boolean isApplyEnabled();

    /**
     * Confirm the dialog by clicking 'Apply' and wait for it to close.
     */
    void hitApply();

    /**
     * Cancel (close) the dialog.
     */
    void cancel();
}
