package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

/**
 * An interface of the multi-select 'Assign Business Term' dialog (Issue #1754) opened from a used
 * ASBIE/BBIE node's in-place 'Business Terms' chip field (the '+' button). It supersedes the
 * standalone 'Assign Business Term' page.
 *
 * <p>The dialog is a modal ({@code mat-dialog-container}); an instance is constructed on an
 * already-shown dialog. It presents a search bar, a selectable results table (with a master
 * checkbox), an optional Type Code input, and Cancel / Assign actions. The primary action reads
 * {@code Assign} for a single selection and {@code Assign (N)} once more than one term is selected.
 */
public interface BieBusinessTermAssignDialog extends Dialog {

    /**
     * Type the given text into the dialog's 'Search by Business Term' search bar.
     *
     * @param businessTerm the business-term text to search for
     */
    void setSearchBusinessTerm(String businessTerm);

    /**
     * Trigger the search (submit the search bar) and wait for the results to settle.
     */
    void hitSearch();

    /**
     * Return the per-row select checkbox for the result row whose Business Term equals the given
     * name.
     *
     * @param businessTerm the Business Term name
     * @return the row's select checkbox element
     */
    WebElement getRowCheckboxByTerm(String businessTerm);

    /**
     * Return the per-row select checkbox of the result row at the given 1-based index.
     *
     * @param index the 1-based row index
     * @return the row's select checkbox element
     */
    WebElement getRowCheckboxAtIndex(int index);

    /**
     * Click the header master checkbox (selects all / clears all).
     */
    void toggleMasterCheckbox();

    /**
     * @return whether the header master checkbox is in the indeterminate (partial-selection) state
     */
    boolean isMasterIndeterminate();

    /**
     * Set the optional Type Code applied to the assignment(s) created on Assign.
     *
     * @param typeCode the type code (max 30 characters)
     */
    void setTypeCode(String typeCode);

    /**
     * @return the primary 'Assign' button element
     */
    WebElement getAssignButton();

    /**
     * @return the text of the primary 'Assign' button (e.g. {@code Assign} or {@code Assign (N)})
     */
    String getAssignButtonText();

    /**
     * Click the primary 'Assign' button and wait for the dialog to close.
     */
    void hitAssign();

    /**
     * Cancel the dialog (the footer 'Cancel' button) and wait for it to close.
     */
    void cancel();
}
