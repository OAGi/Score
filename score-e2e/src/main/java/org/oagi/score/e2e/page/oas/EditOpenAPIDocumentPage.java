package org.oagi.score.e2e.page.oas;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.io.File;

/**
 * An interface of 'Edit OpenAPI Document' page.
 */
public interface EditOpenAPIDocumentPage extends Page {

    /**
     * Return the UI element of the 'OpenAPI Version' select field.
     *
     * @return the UI element of the 'OpenAPI Version' select field
     */
    WebElement getOpenAPIVersionSelectField();

    /**
     * Set the 'OpenAPI Version' select field with the given text.
     *
     * @param openAPIVersion OpenAPI Version
     */
    void setOpenAPIVersion(String openAPIVersion);

    /**
     * Return the UI element of the 'Title' field.
     *
     * @return the UI element of the 'Title' field
     */
    WebElement getTitleField();

    /**
     * Set the 'Title' field with the given text.
     *
     * @param title Title
     */
    void setTitle(String title);

    /**
     * Return the UI element of the 'Document Version' field.
     *
     * @return the UI element of the 'Document Version' field
     */
    WebElement getDocumentVersionField();

    /**
     * Set the 'Document Version' field with the given text.
     *
     * @param documentVersion Document Version
     */
    void setDocumentVersion(String documentVersion);

    /**
     * Return the UI element of the 'Terms of Service' field.
     *
     * @return the UI element of the 'Terms of Service' field
     */
    WebElement getTermsOfServiceField();

    /**
     * Set the 'Terms of Service' field with the given text.
     *
     * @param termsOfService Terms of Service
     */
    void setTermsOfService(String termsOfService);

    /**
     * Return the UI element of the 'Contact Name' field.
     *
     * @return the UI element of the 'Contact Name' field
     */
    WebElement getContactNameField();

    /**
     * Set the 'Contact Name' field with the given text.
     *
     * @param contactName Contact Name
     */
    void setContactName(String contactName);

    /**
     * Return the UI element of the 'Contact URL' field.
     *
     * @return the UI element of the 'Contact URL' field
     */
    WebElement getContactURLField();

    /**
     * Set the 'Contact URL' field with the given text.
     *
     * @param contactURL Contact URL
     */
    void setContactURL(String contactURL);

    /**
     * Return the UI element of the 'Contact Email' field.
     *
     * @return the UI element of the 'Contact Email' field
     */
    WebElement getContactEmailField();

    /**
     * Set the 'Contact Email' field with the given text.
     *
     * @param contactEmail Contact Email
     */
    void setContactEmail(String contactEmail);

    /**
     * Return the UI element of the 'License Name' field.
     *
     * @return the UI element of the 'License Name' field
     */
    WebElement getLicenseNameField();

    /**
     * Set the 'License Name' field with the given text.
     *
     * @param licenseName License Name
     */
    void setLicenseName(String licenseName);

    /**
     * Return the UI element of the 'License URL' field.
     *
     * @return the UI element of the 'License URL' field
     */
    WebElement getLicenseURLField();

    /**
     * Set the 'License URL' field with the given text.
     *
     * @param licenseURL License URL
     */
    void setLicenseURL(String licenseURL);

    /**
     * Return the UI element of the 'Description' field.
     *
     * @return the UI element of the 'Description' field
     */
    WebElement getDescriptionField();

    /**
     * Set the 'Description' field with the given text.
     *
     * @param description Description
     */
    void setDescription(String description);

    /**
     * Return the UI element of the 'Update' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Create' button
     */
    WebElement getUpdateButton(boolean enabled);

    /**
     * Hit the 'Update' button
     */
    void hitUpdateButton();

    /**
     * Return the UI element of the 'Discard' button.
     *
     * @return the UI element of the 'Discard' button
     */
    WebElement getDiscardButton();

    /**
     * Hit the 'Discard' button.
     *
     * @return 'OpenAPI Document' page object
     */
    OpenAPIDocumentPage hitDiscardButton();

    /**
     * Click the 'Discard' button in the top toolbar to open the discard confirmation dialog,
     * without confirming or cancelling it.
     */
    void clickDiscardButtonToOpenDialog();

    /**
     * Return the UI element of the 'Add' button.
     *
     * @return the UI element of the 'Add' button
     */
    WebElement getAddButton();

    /**
     * Return the UI element of the 'Add BIE' button in the 'Endpoint Details' toolbar.
     *
     * @return the UI element of the 'Add BIE' button
     */
    WebElement getAddBIEButton();

    /**
     * Return the UI element of the 'Add Operation' button in the 'Endpoint Details' toolbar.
     *
     * @return the UI element of the 'Add Operation' button
     */
    WebElement getAddOperationButton();

    /**
     * Return the UI element of the 'Generate' button in the 'Endpoint Details' toolbar.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Generate' button
     */
    WebElement getGenerateButton(boolean enabled);

    /**
     * Click the 'Generate' button and wait for the generated OpenAPI document (a {@code .yml} file)
     * to be downloaded.
     *
     * @return the downloaded OpenAPI document file
     */
    File clickGenerateAndDownload();

    /**
     * Click the 'Generate' button without waiting for a download. Used to assert the unsaved-changes
     * guard (Issue #1610): generation is blocked with a snackbar and no file is produced, so the caller
     * reads the snackbar instead of a downloaded file.
     */
    void clickGenerateButton();

    /**
     * Return whether the amber "A Request Body on a DELETE operation is ignored in OpenAPI 3.0.3 ..."
     * banner (Issue #1610) is shown above the 'Endpoint Details' table. It appears only when the document
     * targets an OpenAPI version earlier than 3.1 and at least one loaded operation is a {@code DELETE}
     * carrying a {@code Request} message body.
     *
     * @return {@code true} when the banner is displayed
     */
    boolean isDeleteRequestBodyIgnoredWarningDisplayed();

    /**
     * Open 'Add BIE for OpenAPI Document' dialog.
     *
     * @return 'Add BIE for OpenAPI Document' dialog
     */
    AddBIEForOpenAPIDocumentDialog openAddBIEForOpenAPIDocumentDialog();

    /**
     * Open the 'Add Operation' dialog (for an operation that does not reference a BIE).
     *
     * @return 'Add Operation' dialog
     */
    AddOperationForOpenAPIDocumentDialog openAddOperationDialog();

    /* ----------------------------------------------------- Issue #1729: security */

    /**
     * Return whether the 'Security Schemes' section is displayed.
     *
     * @return {@code true} when displayed
     */
    boolean isSecuritySchemesSectionDisplayed();

    /**
     * Return the UI element of the 'Add Security Scheme' button.
     *
     * @return the 'Add Security Scheme' button element
     */
    WebElement getAddSecuritySchemeButton();

    /**
     * Return whether the 'No scheme configured — the default OAuth 2.0 scheme will be used.' hint is
     * displayed (it is shown only while no scheme is configured).
     *
     * @return {@code true} when displayed
     */
    boolean isNoSchemeHintDisplayed();

    /**
     * Return the 'No scheme configured ...' hint text, or an empty string when absent.
     *
     * @return the hint text
     */
    String getNoSchemeHint();

    /**
     * Open the 'Add Security Scheme' dialog.
     *
     * @return the 'Add Security Scheme' dialog
     */
    OasSecuritySchemeDialog openAddSecuritySchemeDialog();

    /**
     * Return the number of configured security-scheme cards.
     *
     * @return the scheme-card count
     */
    int getSecuritySchemeCardCount();

    /**
     * Return whether a security-scheme card with the given scheme name is displayed.
     *
     * @param schemeName the scheme name
     * @return {@code true} when present
     */
    boolean hasSecuritySchemeCard(String schemeName);

    /**
     * Return the type label shown on a security-scheme card.
     *
     * @param schemeName the scheme name
     * @return the type label
     */
    String getSecuritySchemeCardType(String schemeName);

    /**
     * Return the summary text shown on a security-scheme card.
     *
     * @param schemeName the scheme name
     * @return the summary text
     */
    String getSecuritySchemeCardSummary(String schemeName);

    /**
     * Click a security-scheme card to reopen it in the 'Edit Security Scheme' dialog.
     *
     * @param schemeName the scheme name
     * @return the 'Edit Security Scheme' dialog
     */
    OasSecuritySchemeDialog clickSecuritySchemeCard(String schemeName);

    /**
     * Remove a security-scheme card via its remove icon.
     *
     * @param schemeName the scheme name
     */
    void removeSecuritySchemeCard(String schemeName);

    /**
     * Return the UI element of the 'Document Security' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the 'Document Security' button element
     */
    WebElement getDocumentSecurityButton(boolean enabled);

    /**
     * Return whether the 'Document Security' button is enabled.
     *
     * @return {@code true} when enabled
     */
    boolean isDocumentSecurityButtonEnabled();

    /**
     * Return the document-security summary (the text after 'Document Security: ' on the button).
     *
     * @return the document-security summary (e.g. {@code None})
     */
    String getDocumentSecuritySummary();

    /**
     * Open the 'Document Security' (document-level / global) requirement dialog.
     *
     * @return the requirement dialog
     */
    OasSecurityRequirementDialog openDocumentSecurityDialog();

    /**
     * Open the per-operation 'Operation Security' dialog from an operation row's Security cell.
     *
     * @param tableRecord the operation row
     * @return the requirement dialog
     */
    OasSecurityRequirementDialog openOperationSecurityDialog(WebElement tableRecord);

    /**
     * Return whether an operation row's Security cell shows the non-applicable placeholder ('—').
     *
     * @param tableRecord the operation row
     * @return {@code true} when the Security cell is non-applicable
     */
    boolean isRowSecurityNotApplicable(WebElement tableRecord);

    /**
     * Return the security summary shown on an operation row's Security cell (e.g. {@code Inherited},
     * {@code Public}, or a requirement summary).
     *
     * @param tableRecord the operation row
     * @return the Security cell summary
     */
    String getRowSecuritySummary(WebElement tableRecord);

    WebElement getTableRecordAtIndex(int idx);

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    void toggleSelect(WebElement tableRecord);

    WebElement getRemoveButton(boolean enabled);

    void removeSelectedBIEs();

    /**
     * Set the 'Array Indicator' checkbox of an operation row to the given state.
     *
     * @param tableRecord the operation row
     * @param checked     {@code true} to check (array/list operation), {@code false} to uncheck
     */
    void setRowArrayIndicator(WebElement tableRecord, boolean checked);

    /**
     * Set the 'Suppress Root' checkbox of an operation row to the given state.
     *
     * @param tableRecord the operation row
     * @param checked     {@code true} to check, {@code false} to uncheck
     */
    void setRowSuppressRoot(WebElement tableRecord, boolean checked);

    /**
     * Return whether the 'Suppress Root' checkbox of an operation row is currently checked.
     *
     * @param tableRecord the operation row
     * @return {@code true} when checked
     */
    boolean isRowSuppressRootChecked(WebElement tableRecord);

    /**
     * Return whether the 'Array Indicator' checkbox of an operation row is currently checked.
     *
     * @param tableRecord the operation row
     * @return {@code true} when checked
     */
    boolean isRowArrayIndicatorChecked(WebElement tableRecord);

    /**
     * Return the Verb shown on an operation row.
     *
     * @param tableRecord the operation row
     * @return the Verb (e.g. {@code POST})
     */
    String getRowVerb(WebElement tableRecord);

    /**
     * Set the Verb of an operation row.
     *
     * @param tableRecord the operation row
     * @param verb        the verb (e.g. {@code GET})
     */
    void setRowVerb(WebElement tableRecord, String verb);

    /**
     * Return the Operation ID shown on an operation row.
     *
     * @param tableRecord the operation row
     * @return the Operation ID value
     */
    String getRowOperationId(WebElement tableRecord);

    /**
     * Return the DEN shown on an operation row (empty for a bodyless operation that has no BIE).
     *
     * @param tableRecord the operation row
     * @return the DEN text
     */
    String getRowDen(WebElement tableRecord);

    /**
     * Return the Resource Name shown on an operation row.
     *
     * @param tableRecord the operation row
     * @return the Resource Name value
     */
    String getRowResourceName(WebElement tableRecord);

    /**
     * Set the Resource Name of an operation row.
     *
     * @param tableRecord  the operation row
     * @param resourceName the resource name
     */
    void setRowResourceName(WebElement tableRecord, String resourceName);

    /**
     * Return the Tag Name shown on an operation row.
     *
     * @param tableRecord the operation row
     * @return the Tag Name value
     */
    String getRowTagName(WebElement tableRecord);

    /**
     * Set the Operation ID of an operation row.
     *
     * @param tableRecord the operation row
     * @param operationId the operation ID
     */
    void setRowOperationId(WebElement tableRecord, String operationId);

    /**
     * Return the inline error message currently shown on an operation row's Operation ID field
     * (empty when there is no error).
     *
     * @param tableRecord the operation row
     * @return the inline error message, or an empty string
     */
    String getRowOperationIdError(WebElement tableRecord);

    /**
     * Return whether the 'Array Indicator' control of an operation row is disabled.
     *
     * @param tableRecord the operation row
     * @return {@code true} when disabled
     */
    boolean isRowArrayIndicatorDisabled(WebElement tableRecord);

    /**
     * Return whether the 'Suppress Root' control of an operation row is disabled.
     *
     * @param tableRecord the operation row
     * @return {@code true} when disabled
     */
    boolean isRowSuppressRootDisabled(WebElement tableRecord);

    /**
     * Return whether the 'Message Body' control of an operation row is disabled.
     *
     * @param tableRecord the operation row
     * @return {@code true} when disabled
     */
    boolean isRowMessageBodyDisabled(WebElement tableRecord);

    /* ----------------------------------------------------- Issue #1492 (Option 2): one operation, two bodies */

    /**
     * Return the Message Body shown on an operation row ({@code Request} or {@code Response}).
     *
     * @param tableRecord the operation row
     * @return the Message Body value
     */
    String getRowMessageBody(WebElement tableRecord);

    /**
     * Flip an operation row's inline 'Message Body' dropdown to the given value ({@code Request} or
     * {@code Response}). On an already-saved row this is an in-place body-type conversion: clicking Update
     * must convert the existing body rather than add a second one (no duplicate row).
     *
     * @param tableRecord the operation row
     * @param messageBody {@code Request} or {@code Response}
     */
    void setRowMessageBody(WebElement tableRecord, String messageBody);

    /**
     * Click the 'Update' button WITHOUT asserting the success snackbar. Unlike {@link #hitUpdateButton()}
     * (which asserts {@code Updated}), this is used to exercise the Issue #1492 update-time duplicate-body
     * guard: clicking Update on a document that has two same-type bodies on one {@code (path, verb)} is
     * blocked with a snackbar and no save occurs, so the caller reads {@code getSnackBarMessage(...)}.
     */
    void clickUpdateButton();

    /**
     * Return the inline duplicate-body error message currently shown on an operation row's
     * {@code Message Body} cell (Issue #1492, Option 2), or an empty string when there is no error. The
     * frontend surfaces {@code "Duplicate <Request|Response> body on this endpoint."} on the Message Body
     * cell of a row whose {@code (Resource Name, Verb, Message Body)} slot occurs more than once.
     *
     * @param tableRecord the operation row
     * @return the inline error message, or an empty string
     */
    String getRowMessageBodyError(WebElement tableRecord);

    /**
     * Return whether an operation row is flagged as a duplicate body slot (Issue #1492, Option 2). The
     * frontend renders an {@code .oas-body-slot-error-text} hint on the Verb / Message Body cells of a row
     * whose {@code (Resource Name, Verb, Message Body)} slot occurs more than once among the loaded rows.
     *
     * @param tableRecord the operation row
     * @return {@code true} when the row shows the duplicate-body error
     */
    boolean isRowDuplicateBodyWarningDisplayed(WebElement tableRecord);

    /* ----------------------------------------------------- Issue #1347: error response body type */

    /**
     * Return the body type shown on an operation row's inline Error Response selector
     * ({@code None} | {@code ProblemDetails} | {@code ConfirmMessage}).
     *
     * @param tableRecord the operation row
     * @return the selected body-type label
     */
    String getRowErrorResponseBodyType(WebElement tableRecord);

    /**
     * Return whether the inline Error Response selector of an operation row is enabled (it stays enabled
     * for every operation, including a bodyless one that references no BIE).
     *
     * @param tableRecord the operation row
     * @return {@code true} when enabled
     */
    boolean isRowErrorResponseSelectorEnabled(WebElement tableRecord);

    /**
     * Set an operation row's Error Response body type to {@code No Response Body} or
     * {@code IETF Problem Details}. These commit immediately without opening a dialog. Use
     * {@link #openConfirmMessageDialogViaSelector} for the {@code OAGi Confirm Message} option, which
     * opens the BIE-selection dialog.
     *
     * @param tableRecord the operation row
     * @param label       {@code No Response Body} or {@code IETF Problem Details}
     */
    void setRowErrorResponseBodyType(WebElement tableRecord, String label);

    /**
     * Select the {@code OAGi Confirm Message} option on an operation row's Error Response selector, which
     * opens the 'Select ConfirmMessage BIE' dialog.
     *
     * @param tableRecord the operation row
     * @return the opened dialog
     */
    OasDocConfirmMessageDialog openConfirmMessageDialogViaSelector(WebElement tableRecord);

    /**
     * Re-open the 'Select ConfirmMessage BIE' dialog from an operation row's ConfirmMessage DEN chip
     * (shown once CONFIRM_MESSAGE is selected).
     *
     * @param tableRecord the operation row
     * @return the opened dialog
     */
    OasDocConfirmMessageDialog openConfirmMessageDialogViaChip(WebElement tableRecord);

    /**
     * Return whether an operation row shows the ConfirmMessage DEN chip (present only for CONFIRM_MESSAGE).
     *
     * @param tableRecord the operation row
     * @return {@code true} when the chip is displayed
     */
    boolean isRowConfirmMessageChipDisplayed(WebElement tableRecord);

    /**
     * Return the text of an operation row's ConfirmMessage DEN chip (the picked DEN, or a 'pick' prompt
     * when no BIE has been chosen yet), or an empty string when the chip is absent.
     *
     * @param tableRecord the operation row
     * @return the chip text
     */
    String getRowConfirmMessageChipText(WebElement tableRecord);

}
