package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

/**
 * An interface of the BIE-root 'Add to OpenAPI Document' dialog (Issue #1519).
 *
 * <p>This is the inverse of the OpenAPI-Document-centric 'Add BIE For OpenAPI Document' dialog: it is
 * opened from the BIE-root 'OpenAPI Document Information' panel's header '+' and lets the user bind the
 * current BIE to an OpenAPI Document. The dialog reuses the same assign endpoint as the OpenAPI Document
 * editor, and it previews the Operation ID / Resource Name exactly the way that editor derives them, so
 * the resulting binding is identical regardless of which screen creates it.</p>
 */
public interface BieOpenAPIDocumentAddDialog extends Dialog {

    /**
     * Select an OpenAPI Document by its title in the (searchable) document picker.
     *
     * @param title the OpenAPI Document title
     */
    void selectOpenAPIDocument(String title);

    /**
     * Set the 'Verb' select field.
     *
     * @param verb the HTTP verb (GET, PUT, POST, DELETE, PATCH)
     */
    void setVerb(String verb);

    /**
     * Set the 'Message Body' select field.
     *
     * @param messageBody {@code Request} or {@code Response}
     */
    void setMessageBody(String messageBody);

    /**
     * Return {@code true} if the given 'Message Body' option is disabled (e.g. {@code Request} for a GET).
     *
     * @param messageBody the option label
     * @return {@code true} if disabled, otherwise {@code false}
     */
    boolean isMessageBodyOptionDisabled(String messageBody);

    /**
     * Set the 'Make as an array' checkbox.
     *
     * @param checked target state
     */
    void setArrayIndicator(boolean checked);

    /**
     * Set the 'Suppress a root property' checkbox.
     *
     * @param checked target state
     */
    void setSuppressRoot(boolean checked);

    /**
     * Return the read-only Resource Name preview the dialog derives from the current selection.
     *
     * @return the previewed Resource Name
     */
    String getResourceNamePreview();

    /**
     * Return the read-only Operation ID preview the dialog derives from the current selection.
     *
     * @return the previewed Operation ID
     */
    String getOperationIdPreview();

    /**
     * Return the inline duplicate-body error (Issue #1492) shown when the chosen {@code Verb}/{@code Message
     * Body} would duplicate a body the BIE already has on the selected OpenAPI Document, or an empty string
     * when there is none. This mirrors the OpenAPI Document editor's Add dialog, which likewise flags the
     * duplicate inline and disables {@code Add}.
     *
     * @return the duplicate-body error text, or an empty string
     */
    String getDuplicateBodyError();

    /**
     * Return the UI element of the 'Add' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Add' button
     */
    WebElement getAddButton(boolean enabled);

    /**
     * Click the 'Add' button and wait for the dialog to close.
     */
    void hitAddButton();

    /**
     * Click the 'Cancel' button and close the dialog.
     */
    void cancel();
}
