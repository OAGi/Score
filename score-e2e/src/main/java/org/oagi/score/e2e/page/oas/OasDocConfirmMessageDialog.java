package org.oagi.score.e2e.page.oas;

import org.oagi.score.e2e.page.Dialog;

/**
 * The 'Select Confirm Message BIE' dialog (Issue #1347). It opens from the Edit OpenAPI Document page
 * when an operation's inline Error Response selector is set to {@code ConfirmMessage}. Modeled on the
 * 'Include Meta Header' / 'Pagination Response' BIE pickers, it is locked to a single well-known BIE:
 * the DEN filter is fixed to {@code 'Confirm Message. Confirm Message'} and the search box is disabled,
 * the library is fixed to {@code connectSpec} (no Library selector), and the Branch is locked (disabled)
 * to the release of the document's connected BIE. The author therefore picks among the listed Confirm
 * Message BIEs (distinguished by Business Context). Confirmed with 'Select'; the picked BIE is applied
 * to the operation (and its sibling rows) and persisted later by the page's Update/Generate action.
 */
public interface OasDocConfirmMessageDialog extends Dialog {

    /**
     * Return whether a candidate row with the given DEN is present in the result list (the list is
     * locked to {@code 'Confirm Message. Confirm Message'}, so this confirms a Confirm Message BIE shows).
     *
     * @param den the candidate DEN
     * @return {@code true} when the candidate is listed
     */
    boolean isCandidatePresent(String den);

    /**
     * Return whether a candidate row carrying the given Business Context is present. Because every row
     * shares the locked DEN, the Business Context is what uniquely identifies a specific Confirm Message BIE.
     *
     * @param businessContextName the business context name
     * @return {@code true} when a matching candidate is listed
     */
    boolean isCandidatePresentByBusinessContext(String businessContextName);

    /**
     * Select the candidate row carrying the given Business Context (via its row checkbox).
     *
     * @param businessContextName the business context name that uniquely identifies the Confirm Message BIE
     */
    void selectCandidateByBusinessContext(String businessContextName);

    /**
     * Return whether the 'Select' button is enabled (it is enabled only once a BIE is selected).
     *
     * @return {@code true} when enabled
     */
    boolean isSelectEnabled();

    /**
     * Return whether the Branch field is an ENABLED selector (document-level "apply to all", or a bodyless
     * operation in a multi-release document — Issue #1347). In the per-operation single-release case the
     * Branch is a locked, disabled input instead.
     *
     * @return {@code true} when the Branch is a selectable dropdown
     */
    boolean isBranchSelectorEnabled();

    /**
     * Switch the (enabled) Branch selector to the given release number and wait for the candidate list to
     * reload for that release.
     *
     * @param releaseNum the release number to select
     */
    void selectBranch(String releaseNum);

    /**
     * Confirm the dialog by clicking 'Select' and wait for it to close.
     */
    void hitSelect();

    /**
     * Cancel (close) the dialog without picking a BIE.
     */
    void cancel();
}
