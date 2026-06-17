package org.oagi.score.e2e.page.oas;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * An interface of the 'Add Operation' dialog that defines an API operation (endpoint) which does
 * not reference a BIE (Issue #1730).
 */
public interface AddOperationForOpenAPIDocumentDialog extends Dialog {

    /**
     * Return the UI element of the dialog subtitle.
     *
     * @return the UI element of the dialog subtitle
     */
    WebElement getSubtitle();

    /**
     * Return the UI element of the 'Verb' select field.
     *
     * @return the UI element of the 'Verb' select field
     */
    WebElement getVerbSelectField();

    /**
     * Set the 'Verb' select field with the given value.
     *
     * @param verb the verb (e.g. {@code DELETE} or {@code PATCH})
     */
    void setVerb(String verb);

    /**
     * Return the verb options offered by the 'Verb' select field.
     *
     * @return the verb options
     */
    List<String> getVerbOptions();

    /**
     * Return the UI element of the 'Resource Name (Path)' field.
     *
     * @return the UI element of the 'Resource Name (Path)' field
     */
    WebElement getResourceNameField();

    /**
     * Set the 'Resource Name (Path)' field with the given value.
     *
     * @param resourceName the resource name (path)
     */
    void setResourceName(String resourceName);

    /**
     * Return the UI element of the 'Operation ID' field.
     *
     * @return the UI element of the 'Operation ID' field
     */
    WebElement getOperationIdField();

    /**
     * Return the current value of the 'Operation ID' field.
     *
     * @return the 'Operation ID' value
     */
    String getOperationId();

    /**
     * Set the 'Operation ID' field with the given value.
     *
     * @param operationId the operation ID
     */
    void setOperationId(String operationId);

    /**
     * Return the hint text shown under the 'Operation ID' field.
     *
     * @return the 'Operation ID' hint text
     */
    String getOperationIdHint();

    /**
     * Set the 'Tag (optional)' field with the given value.
     *
     * @param tag the tag
     */
    void setTag(String tag);

    /**
     * Set the 'Summary (optional)' field with the given value.
     *
     * @param summary the summary
     */
    void setSummary(String summary);

    /**
     * Return the UI element of the 'Add' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Add' button
     */
    WebElement getAddButton(boolean enabled);

    /**
     * Click the 'Add' button and wait for the operation to be added.
     */
    void hitAddButton();

    /**
     * Click the 'Cancel' button to close the dialog without adding anything.
     */
    void cancel();
}
